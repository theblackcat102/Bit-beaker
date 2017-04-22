package fi.iki.kuitsi.bitbeaker.fragments.issueContainer;

import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.domainobjects.IssueContainer;
import fi.iki.kuitsi.bitbeaker.fragments.IssueContainerFragment;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.IssueContainersRequest;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.NewIssueContainerRequest;

/**
 * Simple Fragment listing all available milestone for a single repository.
 */
public class MilestonesFragment extends IssueContainerFragment {

	public static MilestonesFragment newInstance(String owner, String repoSlug) {
		MilestonesFragment f = new MilestonesFragment();
		f.setArguments(buildArgumentsBundle(owner, repoSlug));
		return f;
	}

	@Override
	protected IssueContainersRequest createRequest(String owner, String slug) {
		return IssueContainersRequest.milestones(owner, slug);
	}

	@Override
	protected NewIssueContainerRequest createNewRequest(String owner, String slug, String name) {
		return NewIssueContainerRequest.milestone(owner, slug, name);
	}

	@Override
	protected int getEmptyInfo() {
		return R.string.milestones_empty;
	}

	@Override @IssueContainer.Type
	protected String getIssueContainerType() {
		return IssueContainer.MILESTONE;
	}

	@Override
	protected int getNewErrorInfo() {
		return R.string.milestone_new_error;
	}

	@Override
	protected void newIssueContainer() {
		newIssueContainer(R.string.milestone_new, R.string.milestone_new_info,
				R.string.milestone_new);
	}
}
