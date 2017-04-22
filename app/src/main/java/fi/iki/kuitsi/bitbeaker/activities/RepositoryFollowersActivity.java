package fi.iki.kuitsi.bitbeaker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.fragments.UserListFragment;

public class RepositoryFollowersActivity extends BaseRepositoryActivity {

	public RepositoryFollowersActivity() {
		super(R.layout.activity_singlepane_toolbar);
	}

	/**
	 * Create an intent for this activity.
	 */
	public static Intent createIntent(Context context, String owner, String slug) {
		Intent intent = new Intent(context, RepositoryFollowersActivity.class);
		return BaseRepositoryActivity.addExtendedDataToIntent(intent, owner, slug);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.followers);
		setToolbarSubtitle(getOwner() + "/" + getSlug());

		setInitialFragment(R.id.fragment_container, UserListFragment.newInstance(getIntent()),
				UserListFragment.class.getCanonicalName());
	}
}
