package fi.iki.kuitsi.bitbeaker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.fragments.ChangesetListFragment;

public class ChangesetListActivity extends BaseRepositoryActivity {

	public ChangesetListActivity() {
		super(R.layout.activity_singlepane_toolbar);
	}

	public static Intent createIntent(Context context, String owner, String slug) {
		Intent intent = new Intent(context, ChangesetListActivity.class);
		return BaseRepositoryActivity.addExtendedDataToIntent(intent, owner, slug);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.changesets);
		setToolbarSubtitle(getOwner() + "/" + getSlug());

		setInitialFragment(R.id.fragment_container,
				ChangesetListFragment.newInstance(getOwner(), getSlug()),
				ChangesetListFragment.class.getCanonicalName());
	}
}
