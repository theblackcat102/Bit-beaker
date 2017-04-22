package fi.iki.kuitsi.bitbeaker.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fi.iki.kuitsi.bitbeaker.Bitbeaker;
import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.event.RefreshEvent;
import fi.iki.kuitsi.bitbeaker.fragments.IssueListFragment;
import fi.iki.kuitsi.bitbeaker.util.StringUtils;
import fi.iki.kuitsi.bitbeaker.view.AbsListViewScrollDirectionDetector;
import fi.iki.kuitsi.bitbeaker.view.FloatingActionButton;
import fi.iki.kuitsi.bitbeaker.view.MultiSelectionSpinner;

public class IssuesActivity extends BaseRepositoryActivity
		implements SearchView.OnQueryTextListener {

	private static final String TAG = IssuesActivity.class.getSimpleName();

	private static final String QUERY_KEY_STATUS = "status";
	private static final String QUERY_KEY_KIND = "kind";
	private static final String QUERY_KEY_SEARCH = "search";
	private static final int REQUEST_NEW_ISSUE = 0;
	private static final String STATE_STATUS = "IssuesActivity.status_filters";
	private static final String STATE_KIND = "IssuesActivity.kind_filters";
	private static final String STATE_SEARCH = "IssuesActivity.search";
	private static final int TOOLBAR_ANIMATION_DURATION = 250; // ms

	private static final List<String> STATUS_FILTER;
	private static final List<String> KIND_FILTER;

	static {
		STATUS_FILTER = new ArrayList<>();
		STATUS_FILTER.add("new");
		STATUS_FILTER.add("open");
		STATUS_FILTER.add("resolved");
		STATUS_FILTER.add("on hold");
		STATUS_FILTER.add("invalid");
		STATUS_FILTER.add("duplicate");
		STATUS_FILTER.add("wontfix");
		STATUS_FILTER.add("closed");

		KIND_FILTER = new ArrayList<>();
		KIND_FILTER.add("bug");
		KIND_FILTER.add("enhancement");
		KIND_FILTER.add("proposal");
		KIND_FILTER.add("task");
	}

	@BindView(R.id.toolbar_filter) View toolbarView;
	@BindView(R.id.issue_filter_spinner_status) MultiSelectionSpinner statusSpinner;
	@BindView(R.id.issue_filter_spinner_kind) MultiSelectionSpinner kindSpinner;
	@BindView(R.id.add_issue_button) FloatingActionButton fab;

	private ArrayList<String> status = new ArrayList<>();
	private ArrayList<String> kind = new ArrayList<>();
	private List<String> statusOptions = new ArrayList<>(STATUS_FILTER.size());
	private List<String> kindOptions = new ArrayList<>(KIND_FILTER.size());
	private String query;
	private boolean toolbarShown = true;
	private AbsListViewScrollDirectionDetector detector = new AbsListViewScrollDirectionDetector() {
		@Override
		public void onScrollDown() {
			showToolbar(true);
			fab.show();
		}

		@Override
		public void onScrollUp() {
			showToolbar(false);
			fab.hide();
		}
	};

	public IssuesActivity() {
		super(R.layout.activity_issues);
	}

	/**
	 * Create an intent for this activity.
	 */
	public static Intent createIntent(Context context, String owner, String slug) {
		Intent intent = new Intent(context, IssuesActivity.class);
		return BaseRepositoryActivity.addExtendedDataToIntent(intent, owner, slug);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Uri data = getIntent().getData();
		if (data != null) {
			List<String> segments = data.getPathSegments();
			if (segments.size() == 3) {
				Intent intent = createIntent(this, segments.get(0), segments.get(1));
				finish();
				startActivity(intent);
			}
		}

		if (savedInstanceState != null) {
			status = savedInstanceState.getStringArrayList(STATE_STATUS);
			kind = savedInstanceState.getStringArrayList(STATE_KIND);
			query = savedInstanceState.getString(STATE_SEARCH);
		} else {
			final Uri uri = getIntent().getData();
			if (uri == null) {
				// provide some default filters
				status = new ArrayList<>();
				status.add("new");
				status.add("open");
				kind = new ArrayList<>();
				query = null;
			} else {
				status = new ArrayList<>(uri.getQueryParameters(QUERY_KEY_STATUS));
				kind = new ArrayList<>(uri.getQueryParameters(QUERY_KEY_KIND));
				query = uri.getQueryParameter(QUERY_KEY_SEARCH);
			}
		}

		initActionBar();
		initToolbar();

		setInitialFragment(R.id.fragment_container,
				IssueListFragment.newInstance(getOwner(), getSlug()),
				IssueListFragment.class.getCanonicalName());
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		IssueListFragment fragment = findFragmentById(R.id.fragment_container);
		fragment.setPaddingTop(getActionBarHeightPx());
		fragment.setFilterAndReload(query, status, kind);
		enableAutoHide();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArrayList(STATE_STATUS, status);
		outState.putStringArrayList(STATE_KIND, kind);
		outState.putString(STATE_SEARCH, query);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_issues, menu);
		final MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
		final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
		searchView.setQueryHint(getString(R.string.search_issue));
		searchView.setOnQueryTextListener(this);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Bundle b = createBundle();
		switch (item.getItemId()) {
			case R.id.menu_milestones:
				Intent milestones = new Intent(this, MilestonesActivity.class);
				milestones.putExtras(b);
				startActivity(milestones);
				return true;
			case R.id.menu_search:
				onSearchRequested();
				return true;
			case R.id.menu_refresh:
				Bitbeaker.getEventBus().post(new RefreshEvent());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_NEW_ISSUE && resultCode == RESULT_OK) {
			SnackbarManager.show(Snackbar.with(getApplicationContext())
					.text(R.string.issue_submitted), this);
			Bitbeaker.getEventBus().post(new RefreshEvent());
		}
	}

	@Override
	public boolean onQueryTextSubmit(String s) {
		if (StringUtils.isNotEmpty(s)) {
			query = s;
			reloadFragment();
			initActionBar();
		}
		return true;
	}

	@Override
	public boolean onQueryTextChange(String s) {
		if (StringUtils.isEmpty(s)) {
			// Clear search
			query = null;
			reloadFragment();
			initActionBar();
		}
		return false;
	}

	@OnClick(R.id.add_issue_button)
	void addIssue(View view) {
		Intent intent = new Intent(this, NewIssueActivity.class);
		intent.putExtras(createBundle());
		startActivityForResult(intent, REQUEST_NEW_ISSUE);
	}

	private void initActionBar() {
		if (StringUtils.isNotBlank(query)) {
			setTitle(String.format(getString(R.string.search_results_title), query));
		} else {
			setTitle(R.string.issues);
		}
		getSupportActionBar().setSubtitle(getOwner() + "/" + getSlug());
	}

	private void initToolbar() {
		boolean[] selectedStatus = new boolean[STATUS_FILTER.size()];
		for (int i = 0; i < STATUS_FILTER.size(); i++) {
			String s = STATUS_FILTER.get(i);
			statusOptions.add(Helper.translateApiString(s));
			selectedStatus[i] = status.contains(s);
		}
		statusSpinner.setItems(statusOptions, selectedStatus, getString(R.string.issues_menu_any_status),
				new MultiSelectionSpinner.OnItemSelectedListener() {
					@Override
					public void onItemsSelected(boolean allSelected, boolean[] selected) {
						status = new ArrayList<>();
						if (!allSelected) {
							for (int i = 0; i < selected.length; ++i) {
								if (selected[i]) {
									status.add(STATUS_FILTER.get(i));
								}
							}
						}
						Log.d(TAG, "Selected status " + status);
						reloadFragment();
					}
				});

		boolean[] selectedKind = new boolean[KIND_FILTER.size()];
		for (int i = 0; i < KIND_FILTER.size(); i++) {
			String k = KIND_FILTER.get(i);
			kindOptions.add(Helper.translateApiString(k));
			selectedKind[i] = kind.isEmpty() || kind.contains(k);
		}
		kindSpinner.setItems(kindOptions, selectedKind, getString(R.string.issues_menu_all_kind),
				new MultiSelectionSpinner.OnItemSelectedListener() {
					@Override
					public void onItemsSelected(boolean allSelected, boolean[] selected) {
						kind = new ArrayList<>();
						if (!allSelected) {
							for (int i = 0; i < selected.length; ++i) {
								if (selected[i]) {
									kind.add(KIND_FILTER.get(i));
								}
							}
						}
						Log.d(TAG, "Selected kind " + kind);
						reloadFragment();
					}
				});
	}

	private void reloadFragment() {
		IssueListFragment fragment = findFragmentById(R.id.fragment_container);
		fragment.setFilterAndReload(query, status, kind);
		showToolbar(true);
		fab.show();
		detector.reset();
	}

	private int getActionBarHeightPx() {
		TypedValue value = new TypedValue();
		getTheme().resolveAttribute(R.attr.actionBarSize, value, true);
		return getResources().getDimensionPixelSize(value.resourceId);
	}

	private void enableAutoHide() {
		IssueListFragment fragment = findFragmentById(R.id.fragment_container);
		fragment.addOnScrollListener(detector);
	}

	private void showToolbar(boolean show) {
		if (show == toolbarShown) {
			return;
		}
		toolbarShown = show;

		int translationY = show ? 0 : -toolbarView.getBottom();
		toolbarView.animate().setInterpolator(new DecelerateInterpolator())
				.setDuration(TOOLBAR_ANIMATION_DURATION).translationY(translationY);
	}
}
