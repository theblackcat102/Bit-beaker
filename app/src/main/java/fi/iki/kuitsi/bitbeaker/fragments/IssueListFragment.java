package fi.iki.kuitsi.bitbeaker.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.activities.IssueActivity;
import fi.iki.kuitsi.bitbeaker.adapters.IssuesAdapter;
import fi.iki.kuitsi.bitbeaker.data.api.resource.IssueKindResourceProvider;
import fi.iki.kuitsi.bitbeaker.data.api.resource.IssueStatusResourceProvider;
import fi.iki.kuitsi.bitbeaker.domainobjects.Issue;
import fi.iki.kuitsi.bitbeaker.network.ConnectivityChecker;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.IssuesRequest;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.IssueFilterResult;

import java.util.List;

/**
 * A fragment showing a list of issues.
 */
public final class IssueListFragment extends SpiceListFragment {

	private static final int REDUCED_LIMIT = 15;
	private static final int DEFAULT_LIMIT = 20;

	IssuesLoader loader;
	IssuesAdapter adapter;
	private String owner;
	private String slug;

	public IssueListFragment() {
		// Mandatory empty constructor
	}

	/**
	 * Creates a IssueListFragment and sets its arguments.
	 */
	public static IssueListFragment newInstance(String owner, String slug) {
		IssueListFragment fragment = new IssueListFragment();
		Bundle b = new Bundle();
		b.putString("owner", owner);
		b.putString("slug", slug);
		fragment.setArguments(b);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setEndlessScrollingSupport(true);
		setRetainInstance(true);

		// Load arguments
		owner = getArguments().getString("owner");
		slug = getArguments().getString("slug");
		adapter = new IssuesAdapter(getContext(), new IssueStatusResourceProvider(), new IssueKindResourceProvider());
		setAbsListAdapter(adapter);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(R.string.issues_not_found);
	}

	@Override
	public void onListItemClick(AbsListView l, View v, int position, long id) {
		final Issue issue = adapter.getItem(position);
		int localId = issue.getLocalId();
		startActivity(IssueActivity.createIntent(getContext(), owner, slug, localId));
	}

	public void setFilterAndReload(String query, List<String> status, List<String> kind) {
		IssuesRequest.IssueStatusFilter statusFilter = new IssuesRequest.IssueStatusFilter(status);
		IssuesRequest.IssueKindFilter kindFilter = new IssuesRequest.IssueKindFilter(kind);

		IssuesRequest.Builder builder = new IssuesRequest.Builder();
		builder.setAccountName(owner);
		builder.setRepoSlug(slug);
		builder.setSearch(query);
		builder.addFilter(statusFilter);
		builder.addFilter(kindFilter);

		// Hardcoded limits based on the connection
		final ConnectivityChecker.TransferRate transferRate =
				ConnectivityChecker.getNetworkTransferRate(getActivity());
		if (transferRate == ConnectivityChecker.TransferRate.TRANSFER_RATE_SLOW) {
			builder.setLimit(REDUCED_LIMIT);
		} else {
			builder.setLimit(DEFAULT_LIMIT);
		}

		loader = new IssuesLoader(builder);
		setContentLoader(loader);
		loader.load();
	}

	private final class IssuesLoader extends ContentLoader<IssueFilterResult> {
		private final IssuesRequest.Builder builder;
		private IssuesRequest request;
		private boolean hasMore;

		public IssuesLoader(IssuesRequest.Builder builder) {
			this.builder = builder;
		}

		@Override
		IssuesRequest getRequest() {
			request = builder.build();
			return request;
		}

		@Override
		AllIssuesRequestListener getRequestListener() {
			return new AllIssuesRequestListener();
		}

		@Override
		void onLoad() {
			builder.setStart(0);
		}

		@Override
		void onReload() {
			builder.setStart(0);
		}

		@Override
		void onLoadMore() {
			int start = request.getStart() + request.getLimit();
			builder.setStart(start);
		}

		@Override
		boolean hasMore() {
			return hasMore;
		}

		public void setCount(int count) {
			hasMore = count > (request.getStart() + request.getLimit());
			adapter.notifyDataSetChanged();
		}
	}

	final class AllIssuesRequestListener implements RequestListener<IssueFilterResult> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			loader.notifyFinished(spiceException);
		}

		@Override
		public void onRequestSuccess(IssueFilterResult result) {
			if (loader.isLoadingMore()) {
				adapter.add(result.getIssues());
			} else {
				adapter.set(result.getIssues());
			}
			loader.setCount(result.getCount());
			loader.notifyFinished();
		}
	}
}
