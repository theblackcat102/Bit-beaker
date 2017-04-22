package fi.iki.kuitsi.bitbeaker.fragments;

import android.content.Intent;
import android.os.Bundle;

import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.domainobjects.PullRequest;

/**
 * Fragment that displays pull requests. Each type has its own tab.
 */
public class PullRequestFragment extends BaseTabFragment {

	public static PullRequestFragment newInstance(Intent intent) {
		PullRequestFragment fragment = new PullRequestFragment();
		fragment.setArguments(intent.getExtras());
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String owner = getArguments().getString("owner");
		String slug = getArguments().getString("slug");

		addFragment(PullRequestListFragment.newInstance(owner, slug, PullRequest.State.OPEN),
				getString(R.string.api_pullrequest_open));
		addFragment(PullRequestListFragment.newInstance(owner, slug, PullRequest.State.MERGED),
				getString(R.string.api_pullrequest_merged));
		addFragment(PullRequestListFragment.newInstance(owner, slug, PullRequest.State.DECLINED),
				getString(R.string.api_pullrequest_declined));
	}
}
