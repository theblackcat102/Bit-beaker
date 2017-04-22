package fi.iki.kuitsi.bitbeaker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.activities.IssueActivity;
import fi.iki.kuitsi.bitbeaker.adapters.issueContainer.IssuesAdapter;
import fi.iki.kuitsi.bitbeaker.domainobjects.Issue;
import fi.iki.kuitsi.bitbeaker.domainobjects.IssueContainer;
import fi.iki.kuitsi.bitbeaker.network.request.RequestSingleIssue;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.IssuesRequest;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.IssueFilterResult;

/**
 * Fragment that displays all open and new issues for milestone/version/component.
 */
public final class IssueContainerDetailFragment extends SpiceFragment {

	public static final String ISSUE_CONTAINER_ARGS = "issueContainer";
	public static final String ISSUE_CONTAINER_TYPE_ARGS = "type";
	private static final String ISSUE_STATUS_FILTER = "statusFilter";

	@BindView(R.id.listView_issues) ListView list;
	@BindView(R.id.empty_result) View nothingFound;
	private Unbinder unbinder;
	private String owner;
	private String repoSlug;
	private IssuesAdapter adapter;
	private IssuesRequest openIssuesRequest;

	public static IssueContainerDetailFragment newInstance(String owner, String repoSlug,
			String issueContainer, @IssueContainer.Type String type, String[] filter) {
		IssueContainerDetailFragment f = new IssueContainerDetailFragment();

		Bundle args = new Bundle();
		args.putString(IssueContainerFragment.OWNER_ARGS, owner);
		args.putString(IssueContainerFragment.REPO_SLUG_ARGS, repoSlug);
		args.putString(ISSUE_CONTAINER_ARGS, issueContainer);
		args.putString(ISSUE_CONTAINER_TYPE_ARGS, type);
		args.putStringArray(ISSUE_STATUS_FILTER, filter);

		f.setArguments(args);
		return f;

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			owner = getArguments().getString(IssueContainerFragment.OWNER_ARGS);
			repoSlug = getArguments().getString(IssueContainerFragment.REPO_SLUG_ARGS);
			String issueContainer = getArguments().getString(ISSUE_CONTAINER_ARGS);
			//noinspection ResourceType
			@IssueContainer.Type String type = getArguments().getString(ISSUE_CONTAINER_TYPE_ARGS);
			String[] filter = getArguments().getStringArray(ISSUE_STATUS_FILTER);

			IssuesRequest.IssueStatusFilter issueStatusFilter =
					new IssuesRequest.IssueStatusFilter(filter);

			openIssuesRequest = new IssuesRequest.Builder()
					.setAccountName(owner)
					.setRepoSlug(repoSlug)
					.addFilter(issueStatusFilter)
					.addFilter(type, issueContainer).build();
		}

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_issue_container_detail, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		unbinder = ButterKnife.bind(this, view);
	}

	@Override
	public void onResume() {
		super.onResume();
		performRequest();
	}

	@Override
	public void onDestroyView() {
		unbinder.unbind();
		super.onDestroyView();
	}

	private void performRequest() {
		getSpiceManager().execute(openIssuesRequest, openIssuesRequest.getCacheKey(),
				openIssuesRequest.getCacheExpireDuration(), new OpenIssuesRequestListener());
	}

	private void updateIssues(Issue.List issues) {
		if (issues.isEmpty()) {
			list.setVisibility(View.GONE);
			nothingFound.setVisibility(View.VISIBLE);
		} else {
			if (getActivity() != null) {
				adapter = new IssuesAdapter(getActivity(), issues);
				list.setAdapter(adapter);
			}
			nothingFound.setVisibility(View.GONE);
			list.setVisibility(View.VISIBLE);

			list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					openIssueDetails(adapter.getItem(position).getLocalId(),
							adapter.getItem(position).getTitle());
				}
			});

			for (Issue issue : issues) {
				getNumberOfComments(issue);
			}

		}
	}

	private void refreshList() {
		adapter.notifyDataSetChanged();
	}

	private void getNumberOfComments(Issue issue) {
		RequestSingleIssue request = new RequestSingleIssue(owner, repoSlug, issue.getLocalId());
		getSpiceManager().execute(request, request.getCacheKey(), request.getCacheExpireDuration(),
				new SingleIssueRequestListener(issue));
	}

	private void openIssueDetails(int issueId, String title) {
		Bundle b = new Bundle();
		b.putString("slug", repoSlug);
		b.putString("owner", owner);
		b.putString("title", title);
		b.putInt("id", issueId);

		Intent intent = new Intent(getActivity(), IssueActivity.class);
		intent.putExtras(b);
		startActivity(intent);
	}

	private final class SingleIssueRequestListener implements RequestListener<Issue> {

		private final Issue issue;

		public SingleIssueRequestListener(Issue issue) {
			this.issue = issue;
		}

		@Override
		public void onRequestFailure(SpiceException e) {
		}

		@Override
		public void onRequestSuccess(final Issue issue) {
			this.issue.commentCount(issue.getCommentCount());
			refreshList();
		}
	}

	private final class OpenIssuesRequestListener implements RequestListener<IssueFilterResult> {

		@Override
		public void onRequestFailure(SpiceException e) {
		}

		@Override
		public void onRequestSuccess(final IssueFilterResult issues) {
			updateIssues(issues.getIssues());
		}
	}
}
