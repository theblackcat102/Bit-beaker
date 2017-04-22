package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import com.octo.android.robospice.persistence.DurationInMillis;

import fi.iki.kuitsi.bitbeaker.domainobjects.IssueContainer;

public class IssueContainersRequest extends BaseRepositoriesRequest<IssueContainer.List> {

	@IssueContainer.Type private final String type;

	private IssueContainersRequest(@IssueContainer.Type String type, String accountName,
			String repoSlug) {
		super(IssueContainer.List.class, accountName, repoSlug);
		this.type = type;
	}

	public static IssueContainersRequest components(String accountName, String repoSlug) {
		return new IssueContainersRequest(IssueContainer.COMPONENT, accountName, repoSlug);
	}

	public static IssueContainersRequest milestones(String accountName, String repoSlug) {
		return new IssueContainersRequest(IssueContainer.MILESTONE, accountName, repoSlug);
	}

	public static IssueContainersRequest versions(String accountName, String repoSlug) {
		return new IssueContainersRequest(IssueContainer.VERSION, accountName, repoSlug);
	}

	@Override
	public IssueContainer.List loadDataFromNetwork() throws Exception {
		return getService().issueContainers(accountname, slug, type).loadDataFromNetwork();
	}

	@Override
	public String getCacheKey() {
		return type + Integer.valueOf(hashCode());
	}

	@Override
	public long getCacheExpireDuration() {
		return DurationInMillis.ONE_MINUTE;
	}
}
