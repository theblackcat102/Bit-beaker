package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import fi.iki.kuitsi.bitbeaker.domainobjects.IssueContainer;

public final class NewIssueContainerRequest extends BaseRepositoriesRequest<IssueContainer> {

	@IssueContainer.Type private final String type;
	private final String name;

	private NewIssueContainerRequest(@IssueContainer.Type String type, String account, String repoSlug, String name) {
		super(IssueContainer.class, account, repoSlug);
		this.type = type;
		this.name = name;
	}

	public static NewIssueContainerRequest component(String account, String repoSlug, String name) {
		return new NewIssueContainerRequest(IssueContainer.COMPONENT, account, repoSlug, name);
	}

	public static NewIssueContainerRequest milestone(String account, String repoSlug, String name) {
		return new NewIssueContainerRequest(IssueContainer.MILESTONE, account, repoSlug, name);
	}

	public static NewIssueContainerRequest version(String account, String repoSlug, String name) {
		return new NewIssueContainerRequest(IssueContainer.VERSION, account, repoSlug, name);
	}

	@Override
	public IssueContainer loadDataFromNetwork() throws Exception {
		return getService().newIssueContainer(accountname, slug, type, name).loadDataFromNetwork();
	}
}
