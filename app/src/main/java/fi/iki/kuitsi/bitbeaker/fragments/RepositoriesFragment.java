package fi.iki.kuitsi.bitbeaker.fragments;

import android.os.Bundle;

import fi.iki.kuitsi.bitbeaker.R;

public class RepositoriesFragment extends BaseTabFragment {

	public static RepositoriesFragment newInstance() {
		return new RepositoriesFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// User repositories fragment
		addFragment(RepositoryListFragment.newInstance(RepositoryListFragment.USER_REPOSITORIES),
				getString(R.string.repositories));

		// User follow fragment
		addFragment(RepositoryListFragment.newInstance(RepositoryListFragment.USER_FOLLOW),
				getString(R.string.follow));

		// Favorites fragment
		addFragment(new FavoriteListFragment(),
				getString(R.string.favorites));
	}
}
