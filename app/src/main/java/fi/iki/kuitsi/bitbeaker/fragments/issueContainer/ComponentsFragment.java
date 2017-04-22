package fi.iki.kuitsi.bitbeaker.fragments.issueContainer;

import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.domainobjects.IssueContainer;
import fi.iki.kuitsi.bitbeaker.fragments.IssueContainerFragment;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.IssueContainersRequest;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.NewIssueContainerRequest;

/**
 * Simple Fragment listing all available components for a single repository.
 */
public class ComponentsFragment extends IssueContainerFragment {

	public static ComponentsFragment newInstance(String owner, String repoSlug) {
		ComponentsFragment f = new ComponentsFragment();
		f.setArguments(buildArgumentsBundle(owner, repoSlug));
		return f;
	}

	@Override
	protected IssueContainersRequest createRequest(String owner, String slug) {
		return IssueContainersRequest.components(owner, slug);
	}

	@Override
	protected NewIssueContainerRequest createNewRequest(String owner, String slug, String name) {
		return NewIssueContainerRequest.component(owner, slug, name);
	}

	@Override
	protected int getEmptyInfo() {
		return R.string.components_empty;
	}

	@Override @IssueContainer.Type
	protected String getIssueContainerType() {
		return IssueContainer.COMPONENT;
	}

	@Override
	protected int getNewErrorInfo() {
		return R.string.component_new_error;
	}

	@Override
	protected void newIssueContainer() {
		newIssueContainer(R.string.component_new, R.string.component_new_info,
				R.string.component_new);
	}
}
