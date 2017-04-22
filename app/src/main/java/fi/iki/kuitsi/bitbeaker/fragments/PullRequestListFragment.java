package fi.iki.kuitsi.bitbeaker.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.activities.PullRequestCommentActivity;
import fi.iki.kuitsi.bitbeaker.adapters.ParameterizedAdapter;
import fi.iki.kuitsi.bitbeaker.domainobjects.PullRequest;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.PullRequestsRequest;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.PullRequestsResponse;

public class PullRequestListFragment extends SpiceListFragment {
	private static final String TAG = "pullrequests";

	public static PullRequestListFragment newInstance(String owner, String slug,
			PullRequest.State state) {
		PullRequestListFragment fragment = new PullRequestListFragment();
		Bundle args = new Bundle();
		args.putString("owner", owner);
		args.putString("slug", slug);
		args.putInt("state", state.ordinal());
		fragment.setArguments(args);
		return fragment;
	}

	private String owner;
	private String slug;
	private PullRequest.State state;
	private PullRequestAdapter adapter;
	private PullRequestLoader loader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setEndlessScrollingSupport(true);

		// Load arguments
		owner = getArguments().getString("owner");
		slug = getArguments().getString("slug");
		state = PullRequest.State.values()[getArguments().getInt("state")];

		adapter = new PullRequestAdapter(getActivity());
		loader = new PullRequestLoader(owner, slug, state);
		setContentAdapter(adapter, loader);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		int stateStringId;
		if (state == PullRequest.State.OPEN) {
			stateStringId = R.string.no_open_pull_requests;
		} else if (state == PullRequest.State.MERGED) {
			stateStringId = R.string.no_merged_pull_requests;
		} else {
			stateStringId = R.string.no_declined_pull_requests;
		}
		setEmptyText(getString(stateStringId));
	}

	private final class PullRequestLoader extends ContentLoader<PullRequestsResponse> {

		private final PullRequestsRequest request;
		private boolean hasMorePage;

		public PullRequestLoader(String owner, String slug, PullRequest.State state) {
			request = new PullRequestsRequest(owner, slug, state);
			hasMorePage = false;
		}

		@Override
		PullRequestsRequest getRequest() {
			return request;
		}

		@Override
		PullRequestListener getRequestListener() {
			return new PullRequestListener();
		}

		@Override
		public void onLoad() {
			request.setPage(1);
			hasMorePage = false;
		}

		@Override
		public void onReload() {
			request.setPage(1);
			hasMorePage = false;
		}

		@Override
		public boolean hasMore() {
			return hasMorePage;
		}

		@Override
		public void onLoadMore() {
			request.nextPage();
		}

		void setHasMorePage(boolean hasMorePage) {
			this.hasMorePage = hasMorePage;
		}
	}

	private final class PullRequestListener implements RequestListener<PullRequestsResponse> {
		@Override
		public void onRequestFailure(SpiceException spiceException) {
			loader.notifyFinished(spiceException);
		}

		@Override
		public void onRequestSuccess(PullRequestsResponse response) {
			loader.setHasMorePage(response.hasNextPage());
			if (!loader.isLoadingMore()) {
				adapter.clear();
			}
			adapter.addAll(response.getValues());
			loader.notifyFinished();
		}
	}

	@Override
	public void onListItemClick(AbsListView l, View v, int position, long id) {
		PullRequest pullRequest = adapter.getItem(position);
		Bundle b = new Bundle();
		b.putString("slug", slug);
		b.putString("owner", owner);
		b.putInt("pullRequestId", pullRequest.getId());
		Intent intent = new Intent(getActivity(), PullRequestCommentActivity.class);
		intent.putExtras(b);
		startActivity(intent);
	}

	/**
	 * {@link android.widget.ListAdapter} that renders {@link PullRequest}s.
	 */
	private static class PullRequestAdapter extends ParameterizedAdapter<PullRequest> {
		public PullRequestAdapter(Context context) {
			super(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.listitem_two_rows, parent, false);
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.subtitle = (TextView) convertView.findViewById(R.id.subtitle);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final PullRequest pullRequest = getItem(position);

			holder.title.setText("#" + pullRequest.getId() + ": " + pullRequest.getTitle());
			holder.subtitle.setText(pullRequest.getDescription());

			return convertView;
		}

		private static class ViewHolder {
			TextView title;
			TextView subtitle;
		}
	}
}
