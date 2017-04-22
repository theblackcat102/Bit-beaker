package fi.iki.kuitsi.bitbeaker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import fi.iki.kuitsi.bitbeaker.AppComponentService;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.adapters.RepositoryFollowersAdapter;
import fi.iki.kuitsi.bitbeaker.data.ImageLoader;
import fi.iki.kuitsi.bitbeaker.domainobjects.User;
import fi.iki.kuitsi.bitbeaker.network.request.BitbucketRequest;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.RequestRepositoryFollowers;

public class UserListFragment extends SpiceListFragment implements SearchView.OnQueryTextListener,
		MenuItemCompat.OnActionExpandListener {
	private static final String TAG = "users";

	public static UserListFragment newInstance(Intent intent) {
		UserListFragment fragment = new UserListFragment();
		fragment.setArguments(intent.getExtras());
		return fragment;
	}

	private RepositoryFollowersAdapter adapter;
	private UserLoader loader;
	private MenuItem searchItem;

	/**
	 * Mandatory empty constructor.
	 */
	public UserListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		// Load arguments
		String owner = getArguments().getString("owner");
		String slug = getArguments().getString("slug");

		ImageLoader imageLoader = AppComponentService.obtain(getContext().getApplicationContext()).imageLoader();
		adapter = new RepositoryFollowersAdapter(getContext(), imageLoader);
		loader = new UserLoader(owner, slug);
		setContentAdapter(adapter, loader);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.grid_content, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initGrid();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		menuInflater.inflate(R.menu.repository_followers, menu);
		searchItem = menu.findItem(R.id.action_search);
		MenuItemCompat.setOnActionExpandListener(searchItem, this);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		searchView.setOnQueryTextListener(this);
		super.onCreateOptionsMenu(menu, menuInflater);
	}

	@Override
	public boolean onMenuItemActionExpand(MenuItem item) {
		return true;
	}

	@Override
	public boolean onMenuItemActionCollapse(MenuItem item) {
		setFilter(null);
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		setFilter(newText);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		MenuItemCompat.collapseActionView(searchItem);
		return false;
	}

	private void initGrid() {
		AbsListView absListView = getAbsListView();
		if (absListView instanceof GridView) {
			final int gridColumns = getResources().getInteger(R.integer.users_columns);
			final int gridSpacing = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
			Log.d(TAG, "Use grid to display users. Number of columns: " + gridColumns);
			GridView gridView = (GridView) absListView;
			gridView.setNumColumns(gridColumns);
			gridView.setVerticalSpacing(gridSpacing);
			gridView.setHorizontalSpacing(gridSpacing);
		}
	}

	private void setFilter(String constraint) {
		adapter.getFilter().filter(constraint);
	}

	private final class UserLoader extends ContentLoader<User.List> {

		private BitbucketRequest<User.List> request;

		public UserLoader(String owner, String slug) {
			request = new RequestRepositoryFollowers(owner, slug);
		}

		@Override
		BitbucketRequest<User.List> getRequest() {
			return request;
		}

		@Override
		RequestListener<User.List> getRequestListener() {
			return new UserRequestListener();
		}
	}

	private void updateUsers(User.List users) {
		if (getActivity() != null) {
			if (adapter != null) {
				adapter.clear();
				adapter.addAll(users);
			}
		}
	}

	final class UserRequestListener implements RequestListener<User.List> {
		@Override
		public void onRequestFailure(SpiceException e) {
			loader.notifyFinished(e);
		}

		@Override
		public void onRequestSuccess(final User.List users) {
			updateUsers(users);
			loader.notifyFinished();
		}
	}
}
