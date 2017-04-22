package fi.iki.kuitsi.bitbeaker.fragments.issueContainer;

import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.domainobjects.IssueContainer;
import fi.iki.kuitsi.bitbeaker.fragments.IssueContainerFragment;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.IssueContainersRequest;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.NewIssueContainerRequest;

/**
 * Simple Fragment listing all available versions for a single repository.
 */
public class VersionsFragment extends IssueContainerFragment {

	public static VersionsFragment newInstance(String owner, String repoSlug) {
		VersionsFragment f = new VersionsFragment();
		f.setArguments(buildArgumentsBundle(owner, repoSlug));
		return f;
	}

	@Override
	protected IssueContainersRequest createRequest(String owner, String slug) {
		return IssueContainersRequest.versions(owner, slug);
	}

	@Override
	protected NewIssueContainerRequest createNewRequest(String owner, String slug, String name) {
		return NewIssueContainerRequest.version(owner, slug, name);
	}

	@Override
	protected int getEmptyInfo() {
		return R.string.versions_empty;
	}

	@Override @IssueContainer.Type
	protected String getIssueContainerType() {
		return IssueContainer.VERSION;
	}

	@Override
	protected int getNewErrorInfo() {
		return R.string.version_new_error;
	}

	@Override
	protected void newIssueContainer() {
		newIssueContainer(R.string.version_new, R.string.version_new_info,
				R.string.version_new);
	}
}
