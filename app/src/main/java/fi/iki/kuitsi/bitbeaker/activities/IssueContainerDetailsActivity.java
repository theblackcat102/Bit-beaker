package fi.iki.kuitsi.bitbeaker.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.ArrayAdapter;

import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.TwoWayMap;
import fi.iki.kuitsi.bitbeaker.domainobjects.IssueContainer;
import fi.iki.kuitsi.bitbeaker.fragments.IssueContainerDetailFragment;
import fi.iki.kuitsi.bitbeaker.fragments.IssueContainerFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IssueContainerDetailsActivity extends BaseActivity implements
		ActionBar.OnNavigationListener {

	private static final TwoWayMap<Set<String>, Integer> FILTER_POSITIONS;

	static {
		FILTER_POSITIONS = new TwoWayMap<>(Collections.<String>emptySet(), -1);
		FILTER_POSITIONS.put(new HashSet<>(Arrays.asList("new", "open")), 0);
		FILTER_POSITIONS.put(new HashSet<>(Arrays.asList("resolved")), 1);
		FILTER_POSITIONS.put(new HashSet<>(Arrays.asList("on hold")), 2);
		FILTER_POSITIONS.put(new HashSet<>(Arrays.asList("invalid")), 3);
		FILTER_POSITIONS.put(new HashSet<>(Arrays.asList("duplicate")), 4);
		FILTER_POSITIONS.put(new HashSet<>(Arrays.asList("wontfix")), 5);
		FILTER_POSITIONS.put(new HashSet<>(Arrays.asList("closed")), 6);
	}

	private String slug;
	private String owner;
	private String issueContainer;
	@IssueContainer.Type private String type;

	private boolean spinnerFirstTime = true;
	private String[] filter;
	private int pos; // position in the action bar navigation
	private List<String> statusOptions = new ArrayList<>();
	private ArrayAdapter<String> statusSpinnerArrayAdapter;

	public IssueContainerDetailsActivity() {
		super(R.layout.activity_singlepane);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle b = getIntent().getExtras();
		slug = b.getString(IssueContainerFragment.REPO_SLUG_ARGS);
		owner = b.getString(IssueContainerFragment.OWNER_ARGS);
		issueContainer = b.getString(IssueContainerDetailFragment.ISSUE_CONTAINER_ARGS);
		//noinspection ResourceType
		type = b.getString(IssueContainerDetailFragment.ISSUE_CONTAINER_TYPE_ARGS);


		if (savedInstanceState != null) {
			pos = savedInstanceState.getInt("pos");
			filter = savedInstanceState.getStringArray("filter");
		} else {
			final String[] statusFilter = b.getStringArray("statusFilter");
			pos = getPositionFromFilter(statusFilter);
			if (pos == -1) {
				filter = statusFilter;
			} else {
				updateFilter();
			}
		}

		initActionBar();

		setInitialFragment(R.id.fragment_container,
				IssueContainerDetailFragment.newInstance(owner, slug, issueContainer, type, filter),
				IssueContainerDetailFragment.class.getCanonicalName());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArray("filter", filter);
		outState.putInt("pos", pos);
	}

	@Override
	public boolean onNavigationItemSelected(int selectedPos, long itemId) {
		pos = selectedPos;
		if (spinnerFirstTime) {
			spinnerFirstTime = false;
			return false;
		}
		removeCustomQueryEntry();
		updateFilter();

		replaceFragment(R.id.fragment_container,
				IssueContainerDetailFragment.newInstance(owner, slug, issueContainer, type, filter),
				IssueContainerDetailFragment.class.getCanonicalName());
		return true;
	}

	private void initActionBar() {
		statusOptions.add(Helper.translateApiString("new") + " & "
				+ Helper.translateApiString("open"));
		statusOptions.add(Helper.translateApiString("resolved"));
		statusOptions.add(Helper.translateApiString("on hold"));
		statusOptions.add(Helper.translateApiString("invalid"));
		statusOptions.add(Helper.translateApiString("duplicate"));
		statusOptions.add(Helper.translateApiString("wontfix"));
		statusOptions.add(Helper.translateApiString("closed"));

		if (pos < 0) {
			// This query was probably started with a custom URL.
			statusOptions.add(getString(R.string.issues_menu_custom_query));
			pos = statusOptions.size() - 1;
		}

		statusSpinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, statusOptions);
		statusSpinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);

		ActionBar actionBar = getSupportActionBar();
		setTitle(owner + "/" + slug);
		actionBar.setSubtitle(issueContainer);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(statusSpinnerArrayAdapter, this);
		actionBar.setSelectedNavigationItem(pos);
	}

	private void updateFilter() {
		final Set<String> filterSet = FILTER_POSITIONS.getByValue(pos);
		filter = new String[filterSet.size()];
		filterSet.toArray(filter);

		Log.d("Issues", "Selected filter [" + pos + "] " + filterSet);
	}

	/**
	 * @param statusValues The list of "status" parameters in the URL query string.
	 * @return the position in the action bar navigation spinner that
	 * corresponds to the filter. -1 if unknown.
	 */
	private int getPositionFromFilter(final String[] statusValues) {
		if (statusValues != null) {
			Set<String> statusValuesSet = new HashSet<>(Arrays.asList(statusValues));
			return FILTER_POSITIONS.getByKey(statusValuesSet);
		}
		return 0;
	}

	/**
	 * Removes the Custom query -option from the list of filters
	 * (if such an entry exists at the end of the list).
	 */
	private void removeCustomQueryEntry() {
		final String lastStatus = statusOptions.get(statusOptions.size() - 1);
		if (getString(R.string.issues_menu_custom_query).equals(lastStatus)) {
			statusOptions.remove(statusOptions.size() - 1);
			statusSpinnerArrayAdapter.remove(lastStatus);
			statusSpinnerArrayAdapter.notifyDataSetChanged();
		}
	}

}
