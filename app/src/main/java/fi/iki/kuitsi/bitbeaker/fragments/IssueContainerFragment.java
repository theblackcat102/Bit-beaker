package fi.iki.kuitsi.bitbeaker.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.SpiceServiceListener;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.activities.IssueContainerDetailsActivity;
import fi.iki.kuitsi.bitbeaker.adapters.IssueContainerAdapter;
import fi.iki.kuitsi.bitbeaker.domainobjects.IssueContainer;
import fi.iki.kuitsi.bitbeaker.domainobjects.Privilege;
import fi.iki.kuitsi.bitbeaker.domainobjects.User;
import fi.iki.kuitsi.bitbeaker.network.SpiceServiceListenerAdapter;
import fi.iki.kuitsi.bitbeaker.network.request.BitbucketRequest;
import fi.iki.kuitsi.bitbeaker.network.request.RequestPrivileges;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.IssuesRequest;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.NewIssueContainerRequest;
import fi.iki.kuitsi.bitbeaker.network.request.user.RequestUser;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.IssueFilterResult;
import fi.iki.kuitsi.bitbeaker.network.response.user.UserEndpoint;
import fi.iki.kuitsi.bitbeaker.viewmodel.IssueContainerStat;

import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public abstract class IssueContainerFragment extends SpiceFragment {

	public static final String OWNER_ARGS = "owner";
	public static final String REPO_SLUG_ARGS = "repoSlug";

	private final SpiceServiceListener spiceServiceListener =
			new SpiceServiceListenerAdapter() {
				@Override
				protected void onIdle() {
					if (refreshItem != null) {
						MenuItemCompat.setActionView(refreshItem, null);
					}
				}

				@Override
				protected void onActive() {
					if (refreshItem != null) {
						MenuItemCompat.setActionView(refreshItem, R.layout.actionview_loading);
					}
				}
			};

	private String owner;
	private String repoSlug;
	private MenuItem refreshItem;
	private boolean displayMenuAdd = false;
	private ListView list;
	private IssueContainerAdapter adapter;
	private View nothingFound;

	protected static Bundle buildArgumentsBundle(String owner, String slug) {
		Bundle arguments = new Bundle();
		arguments.putString(OWNER_ARGS, owner);
		arguments.putString(REPO_SLUG_ARGS, slug);
		return arguments;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			owner = getArguments().getString(OWNER_ARGS);
			repoSlug = getArguments().getString(REPO_SLUG_ARGS);
		}

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_milestones, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		list = ButterKnife.findById(view, R.id.listView);
		nothingFound = ButterKnife.findById(view, R.id.empty_result);
		TextView emptyText = ButterKnife.findById(view, R.id.empty_info);
		emptyText.setText(getEmptyInfo());
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final IssueContainerStat issueContainer = adapter.getItem(position);
				Intent intent = new Intent(getActivity(), IssueContainerDetailsActivity.class);
				intent.putExtra(REPO_SLUG_ARGS, repoSlug);
				intent.putExtra(OWNER_ARGS, owner);
				intent.putExtra(IssueContainerDetailFragment.ISSUE_CONTAINER_ARGS,
						issueContainer.getName());
				intent.putExtra(IssueContainerDetailFragment.ISSUE_CONTAINER_TYPE_ARGS,
						getIssueContainerType());
				startActivity(intent);
			}
		});
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		adapter = new IssueContainerAdapter(getActivity());
		list.setAdapter(adapter);
	}

	@Override
	public void onStart() {
		super.onStart();
		performRequest();
		performUserRequest();
	}

	@Override
	public void onResume() {
		super.onResume();
		getSpiceManager().addSpiceServiceListener(spiceServiceListener);
	}

	@Override
	public void onPause() {
		getSpiceManager().removeSpiceServiceListener(spiceServiceListener);
		super.onPause();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_milestones, menu);
		refreshItem = menu.findItem(R.id.menu_refresh);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_add).setVisible(displayMenuAdd);
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_add:
				newIssueContainer();
				return true;
			case R.id.menu_refresh:
				performRequest();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void performRequest() {
		BitbucketRequest<IssueContainer.List> request = createRequest(owner, repoSlug);
		if (!isVisible()) {
			request.setPriority(SpiceRequest.PRIORITY_LOW);
		}
		getSpiceManager().execute(request, request.getCacheKey(), request.getCacheExpireDuration(),
				new IssueContainerRequestListener());
	}

	private void performUserRequest() {
		RequestUser request = new RequestUser();
		getSpiceManager().execute(request, request.getCacheKey(), request.getCacheExpireDuration(),
				new UserRequestListener());
	}

	private void performPrivilegeRequest(String username) {
		RequestPrivileges requestPrivileges = new RequestPrivileges(owner, repoSlug, username);
		getSpiceManager().execute(requestPrivileges, requestPrivileges.getCacheKey(),
				requestPrivileges.getCacheExpireDuration(), new PrivilegeRequestListener());
	}

	private void performNewRequest(CharSequence title) {
		NewIssueContainerRequest request = createNewRequest(owner, repoSlug,
				title.toString());
		getSpiceManager().execute(request, new NewIssueContainerRequestListener(getNewErrorInfo()));
	}

	private void maybeInvalidateMenu() {
		ActivityCompat.invalidateOptionsMenu(getActivity());
	}

	private void updateData(IssueContainer.List issueContainers) {
		if (issueContainers.isEmpty()) {
			list.setVisibility(View.GONE);
			nothingFound.setVisibility(View.VISIBLE);
		} else if (adapter != null) {
			adapter.clear();
			for (IssueContainer issueContainer : issueContainers) {
				IssueContainerStat stat = IssueContainerStat.wrap(issueContainer);
				loadIssues(stat);
				adapter.add(stat);
			}
			list.setVisibility(View.VISIBLE);
			nothingFound.setVisibility(View.GONE);
		}
	}

	private void loadIssues(IssueContainerStat issueContainer) {
		IssuesRequest.Builder builder = new IssuesRequest.Builder()
				.setAccountName(owner)
				.setRepoSlug(repoSlug)
				.setLimit(0)
				.addFilter(getIssueContainerType(), issueContainer.getName());
		IssuesRequest request = builder.build();
		getSpiceManager().execute(request, new AllIssuesRequestListener(issueContainer));

		builder = new IssuesRequest.Builder()
				.setAccountName(owner)
				.setRepoSlug(repoSlug)
				.setLimit(0)
				.addFilter(getIssueContainerType(), issueContainer.getName())
				.addFilter(IssuesRequest.RESOLVED);
		request = builder.build();
		getSpiceManager().execute(request, new ClosedIssuesRequestListener(issueContainer));
	}

	protected void newIssueContainer(@StringRes int titleResource, @StringRes int infoResource,
			@StringRes int positiveButtonResource) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = LayoutInflater.from(getActivity());
		@SuppressLint("InflateParams")
		View view = inflater.inflate(R.layout.dialog_new_issue_container, null);

		builder.setTitle(titleResource);
		builder.setView(view);

		final EditText text = (EditText) view.findViewById(R.id.new_name);
		((TextView) view.findViewById(R.id.create_new_info)).setText(infoResource);

		builder.setPositiveButton(positiveButtonResource, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				performNewRequest(text.getText());
			}
		});

		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		builder.create().show();
	}

	protected abstract BitbucketRequest<IssueContainer.List> createRequest(String owner, String slug);

	protected abstract NewIssueContainerRequest createNewRequest(String owner, String slug,
			String name);

	protected abstract int getEmptyInfo();

	@IssueContainer.Type protected abstract String getIssueContainerType();

	protected abstract int getNewErrorInfo();

	protected abstract void newIssueContainer();

	private final class PrivilegeRequestListener implements RequestListener<Privilege.List> {
		@Override
		public void onRequestFailure(SpiceException e) {
			displayMenuAdd = false;
			maybeInvalidateMenu();
		}

		@Override
		public void onRequestSuccess(final Privilege.List privilege) {
			// Right now this indicates the current user has administrative rights on the
			// current looked at repository, so the user is allowed to create milestones...
			displayMenuAdd = true;
			maybeInvalidateMenu();
		}
	}

	private final class UserRequestListener implements RequestListener<UserEndpoint> {
		@Override
		public void onRequestFailure(SpiceException e) {
		}

		@Override
		public void onRequestSuccess(final UserEndpoint userEndpoint) {
			if (userEndpoint != null) {
				final User user = userEndpoint.getUser();
				if (user.getUsername().equalsIgnoreCase(owner)) {
					// The currently logged in user is the owner, of course the owner is allowed
					// to create new milestones.
					displayMenuAdd = true;
					maybeInvalidateMenu();
				} else {
					performPrivilegeRequest(user.getUsername());
				}
			}
		}
	}

	private final class IssueContainerRequestListener implements RequestListener<IssueContainer.List> {
		@Override
		public void onRequestFailure(SpiceException e) {
			Crouton.showText(getActivity(), R.string.milestones_loading_error, Style.ALERT);
		}

		@Override
		public void onRequestSuccess(IssueContainer.List issueContainers) {
			updateData(issueContainers);
		}
	}

	private final class ClosedIssuesRequestListener implements RequestListener<IssueFilterResult> {
		private final IssueContainerStat container;

		public ClosedIssuesRequestListener(IssueContainerStat c) {
			this.container = c;
		}

		@Override
		public void onRequestFailure(SpiceException e) {
			Crouton.showText(getActivity(), R.string.milestones_loading_error, Style.ALERT);
		}

		@Override
		public void onRequestSuccess(final IssueFilterResult issues) {
			container.closedIssueCount(issues.getCount());
			adapter.notifyDataSetChanged();
		}
	}

	private final class AllIssuesRequestListener implements RequestListener<IssueFilterResult> {
		private final IssueContainerStat container;

		public AllIssuesRequestListener(IssueContainerStat c) {
			this.container = c;
		}

		@Override
		public void onRequestFailure(SpiceException e) {
			Crouton.showText(getActivity(), R.string.milestones_loading_error, Style.ALERT);
		}

		@Override
		public void onRequestSuccess(final IssueFilterResult issues) {
			container.issueCount(issues.getCount());
			adapter.notifyDataSetChanged();
		}
	}

	private final class NewIssueContainerRequestListener
			implements RequestListener<IssueContainer> {
		@StringRes
		final int errorMessageResource;

		public NewIssueContainerRequestListener(int errorMessageResource) {
			this.errorMessageResource = errorMessageResource;
		}

		@Override
		public void onRequestFailure(SpiceException e) {
			Crouton.showText(getActivity(), errorMessageResource, Style.ALERT);
		}

		@Override
		public void onRequestSuccess(IssueContainer issueContainer) {
			IssueContainerStat stat = IssueContainerStat.wrap(issueContainer);
			stat.issueCount(0);
			stat.closedIssueCount(0);
			adapter.add(stat);
		}
	}
}
