package fi.iki.kuitsi.bitbeaker.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.activities.ChangesetActivity;
import fi.iki.kuitsi.bitbeaker.adapters.ChangesetsAdapter;
import fi.iki.kuitsi.bitbeaker.domainobjects.Changeset;
import fi.iki.kuitsi.bitbeaker.network.ConnectivityChecker;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.ChangesetsRequest;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.TagsRequest;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.Tags;
import fi.iki.kuitsi.bitbeaker.util.StringUtils;

import java.util.ArrayList;

/**
 * Display repository's commits.
 */
public class ChangesetListFragment extends SpiceListFragment {

	private static final int REDUCED_LIMIT = 10;
	private static final int DEFAULT_LIMIT = 15;

	/**
	 * Creates a ChangesetListFragment and sets its arguments.
	 *
	 * @param owner Owner of the repository
	 * @param slug Repository identifier
	 */
	public static ChangesetListFragment newInstance(String owner, String slug) {
		ChangesetListFragment fragment = new ChangesetListFragment();
		fragment.setArguments(buildArgumentsBundle(owner, slug));
		return fragment;
	}

	private static Bundle buildArgumentsBundle(String owner, String slug) {
		Bundle arguments = new Bundle();
		arguments.putString("owner", owner);
		arguments.putString("slug", slug);
		return arguments;
	}

	private String owner;
	private String slug;
	private ChangesetsLoader loader;
	private ChangesetsAdapter adapter;

	/**
	 * Mandatory empty constructor.
	 */
	public ChangesetListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setEndlessScrollingSupport(true);

		// Load arguments
		owner = getArguments().getString("owner");
		slug = getArguments().getString("slug");

		// Hardcoded limits based on the connection
		int limit = DEFAULT_LIMIT;
		final ConnectivityChecker.TransferRate transferRate =
				ConnectivityChecker.getNetworkTransferRate(getActivity());
		if (transferRate == ConnectivityChecker.TransferRate.TRANSFER_RATE_SLOW) {
			limit = REDUCED_LIMIT;
		}

		adapter = new ChangesetsAdapter(getActivity(), owner, slug);
		loader = new ChangesetsLoader(owner, slug, limit);
		TagsRequest tagsRequest = new TagsRequest(owner, slug);
		tagsRequest.setPriority(SpiceRequest.PRIORITY_LOW);
		getSpiceManager().execute(tagsRequest, tagsRequest.getCacheKey(),
				tagsRequest.getCacheExpireDuration(), new TagsRequestListener());
		setContentAdapter(adapter, loader);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getString(R.string.no_changesets_error));
	}

	@Override
	public void onListItemClick(AbsListView l, View v, int position, long id) {
		final Changeset changeset = adapter.getItem(position);
		ArrayList<String> tags = adapter.getTags(position);
		startActivity(ChangesetActivity.createIntent(getActivity(), owner, slug, changeset, tags));
	}

	private final class ChangesetsLoader extends ContentLoader<Changeset.List> {

		private final ChangesetsRequest request;

		public ChangesetsLoader(String owner, String slug, int limit) {
			request = new ChangesetsRequest(owner, slug, limit);
		}

		@Override
		ChangesetsRequest getRequest() {
			return request;
		}

		@Override
		RequestListener<Changeset.List> getRequestListener() {
			return new ChangesetsRequestListener();
		}

		@Override
		void onLoad() {
			request.setStart(null);
		}

		@Override
		void onReload() {
			request.setStart(null);
		}

		@Override
		public boolean hasMore() {
			String start = adapter.getNextChangeset();
			return StringUtils.isNotBlank(start);
		}

		@Override
		public void onLoadMore() {
			String start = adapter.getNextChangeset();
			request.setStart(start);
		}
	}

	private void populateList(Changeset.List changesets) {
		if (getActivity() != null) {
			if (adapter != null) {
				if (!loader.isLoadingMore()) {
					adapter.clear();
				}
				adapter.addAll(changesets);
			}
		}
	}

	private final class ChangesetsRequestListener implements RequestListener<Changeset.List> {
		@Override
		public void onRequestFailure(SpiceException e) {
			loader.notifyFinished(e);
		}

		@Override
		public void onRequestSuccess(final Changeset.List changesets) {
			populateList(changesets);
			loader.notifyFinished();
		}
	}

	private final class TagsRequestListener implements RequestListener<Tags> {
		@Override
		public void onRequestFailure(SpiceException e) {
		}

		@Override
		public void onRequestSuccess(final Tags tagList) {
			adapter.setTags(tagList);
		}
	}
}
