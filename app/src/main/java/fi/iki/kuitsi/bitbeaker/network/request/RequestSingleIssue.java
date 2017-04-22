package fi.iki.kuitsi.bitbeaker.network.request;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.domainobjects.Issue;

public class RequestSingleIssue extends BitbucketRequest<Issue> {

	private static final String CACHE = "singleIssue";

	private String accountName;
	private String repoSlug;
	private int issueId;

	public RequestSingleIssue(String accountName, String repoSlug, int issueId) {
		super(Issue.class);
		this.accountName = accountName;
		this.repoSlug = repoSlug;
		this.issueId = issueId;
	}

	@Override
	public String getCacheKey() {
		return accountName + "_" + repoSlug + "_" + CACHE + "_" + issueId;
	}

	@Override
	public long getCacheExpireDuration() {
		return DurationInMillis.ALWAYS_EXPIRED;
	}

	@Override
	public Issue loadDataFromNetwork() throws Exception {
		return getService().singleIssue(accountName, repoSlug, issueId).loadDataFromNetwork();
	}
}
