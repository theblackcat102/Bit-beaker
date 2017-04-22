package fi.iki.kuitsi.bitbeaker.fragments;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.adapters.RepositoriesAdapter;
import fi.iki.kuitsi.bitbeaker.adapters.RepositoriesAdapter.Sort;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;
import fi.iki.kuitsi.bitbeaker.network.request.BitbucketRequest;
import fi.iki.kuitsi.bitbeaker.network.request.user.RequestUserFollow;
import fi.iki.kuitsi.bitbeaker.network.request.user.RequestUserRepositories;
import fi.iki.kuitsi.bitbeaker.preferences.IntPreference;
import fi.iki.kuitsi.bitbeaker.preferences.PreferencesModule;
import fi.iki.kuitsi.bitbeaker.provider.BitbeakerContract;

/**
 * A {@linkplain SpiceListFragment} showing a list of repositories.
 */
public class RepositoryListFragment extends SpiceListFragment {

	public static final int USER_REPOSITORIES = 1;
	public static final int USER_FOLLOW = 2;

	private static final int ID_GROUP_SORT = 1;

	private IntPreference sortOrder;
	private RepositoriesLoader loader;
	private RepositoriesAdapter adapter;
	private final ContentObserver observer = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			onChange(selfChange, null);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			if (isAdded() && adapter != null) {
				adapter.notifyDataSetChanged();
			}
		}
	};

	public RepositoryListFragment() {
		// Mandatory empty constructor
	}

	/**
	 * Creates a IssueListFragment and sets its arguments.
	 *
	 * @param mode Mode can be {@link #USER_REPOSITORIES} or {@link #USER_FOLLOW}
	 */
	public static RepositoryListFragment newInstance(int mode) {
		RepositoryListFragment fragment = new RepositoryListFragment();
		fragment.setArguments(buildArgumentsBundle(mode));
		return fragment;
	}

	private static Bundle buildArgumentsBundle(int mode) {
		Bundle arguments = new Bundle();
		arguments.putInt("mode", mode);
		return arguments;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		context.getContentResolver().registerContentObserver(BitbeakerContract.Repository
				.CONTENT_STARRED_URI, true, observer);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load arguments
		BitbucketRequest<Repository.List> repositoryRequest;
		int mode = getArguments().getInt("mode", -1);
		if (mode == USER_REPOSITORIES) {
			repositoryRequest = new RequestUserRepositories();
		} else if (mode == USER_FOLLOW) {
			repositoryRequest = new RequestUserFollow();
		} else {
			throw new UnsupportedOperationException("Unknown mode for " + mode);
		}
		loader = new RepositoriesLoader(repositoryRequest);
		adapter = new RepositoriesAdapter(getActivity());
		setContentAdapter(adapter, loader);

		setHasOptionsMenu(true);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getString(R.string.no_repositories_found));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		sortOrder = PreferencesModule.provideRepositorySortOrder(getActivity());
	}

	@Override
	public void onDetach() {
		super.onDetach();
		getActivity().getContentResolver().unregisterContentObserver(observer);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		SubMenu sub = menu.addSubMenu(R.string.repositories_sorting_order);
		sub.setIcon(R.drawable.ab_icon_sort);
		MenuItemCompat.setShowAsAction(sub.getItem(), MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		for (int i = 0; i < Sort.values().length; i++) {
			sub.add(ID_GROUP_SORT, i, Menu.NONE, Sort.values()[i].toString(getResources()));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getGroupId() == ID_GROUP_SORT) {
			adapter.sort(Sort.values()[item.getItemId()]);

			sortOrder.set(item.getItemId());

			return true;
		}
		return super.onOptionsItemSelected(item);

	}

	private class RepositoriesLoader extends ContentLoader<Repository.List> {
		private final BitbucketRequest<Repository.List> request;

		public RepositoriesLoader(BitbucketRequest<Repository.List> request) {
			this.request = request;
		}

		@Override
		BitbucketRequest<Repository.List> getRequest() {
			return request;
		}

		@Override
		RequestListener<Repository.List> getRequestListener() {
			return new RepositoryRequestListener();
		}
	}

	private final class RepositoryRequestListener implements RequestListener<Repository.List> {
		@Override
		public void onRequestFailure(SpiceException e) {
			loader.notifyFinished(e);
		}

		@Override
		public void onRequestSuccess(final Repository.List repositories) {
			adapter.clear();
			adapter.addAll(repositories);

			int index;
			try {
				index = sortOrder.get();
			} catch (ClassCastException e) {
				// Old preference from v3.0.2 found, just ignore it.
				index = -1;
			}
			if (index >= Sort.values().length || index < 0) {
				index = Sort.UPDATED_DESC.ordinal();
			}
			adapter.sort(Sort.values()[index]);

			loader.notifyFinished();
		}
	}
}
