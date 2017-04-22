package fi.iki.kuitsi.bitbeaker.activities;

import android.os.Bundle;

import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.fragments.PullRequestCommentListFragment;

/**
 * Activity that hosts a fragment that display comments of a pull request.
 */
public class PullRequestCommentActivity extends BaseRepositoryActivity {

	public PullRequestCommentActivity() {
		super(R.layout.activity_singlepane_toolbar);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.pull_request_comments);
		setToolbarSubtitle(getOwner() + "/" + getSlug());

		setInitialFragment(R.id.fragment_container,
				PullRequestCommentListFragment.newInstance(getIntent()),
				PullRequestCommentListFragment.class.getCanonicalName());
	}

}
