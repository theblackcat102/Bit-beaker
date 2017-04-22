package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.domainobjects.PullRequest;
import fi.iki.kuitsi.bitbeaker.network.request.BitbucketRequest;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.PullRequestsResponse;
import fi.iki.kuitsi.bitbeaker.util.Objects;

/**
 * Get a list of of a repository's pull requests.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/pullrequests+Resource
 * #pullrequestsResource-GETalistofopenpullrequests">
 * pullrequests Resource - Bitbucket - Atlassian Documentation</a>
 */
public class PullRequestsRequest extends BitbucketRequest<PullRequestsResponse> {

	private final String accountname;
	private final String slug;
	private final PullRequest.State state;
	private int page;

	/**
	 * Construct a pull requests list request associated with the specified repository.
	 * By default, only the open pull requests are returned.
	 *
	 * @param accountname The team or individual account owning the repo
	 * @param slug The repo identifier
	 */
	public PullRequestsRequest(String accountname, String slug) {
		this(accountname, slug, null);
	}

	/**
	 * Construct a pull requests list request associated with the specified repository.
	 *
	 * @param accountname The team or individual account owning the repo
	 * @param slug The repo identifier
	 * @param state
	 */
	public PullRequestsRequest(String accountname, String slug, PullRequest.State state) {
		super(PullRequestsResponse.class);
		this.accountname = accountname;
		this.slug = slug;
		this.state = state;
		this.page = 1;
	}

	public int getPage() {
		return page;
	}

	public void nextPage() {
		page++;
	}

	public void setPage(int page) {
		this.page = page;
	}

	@Override
	public PullRequestsResponse loadDataFromNetwork() throws Exception {
		return getService().pullrequests(accountname, slug, state, page).loadDataFromNetwork();
	}

	@Override
	public String getCacheKey() {
		return "pullrequests" + String.valueOf(hashCode());
	}

	@Override
	public long getCacheExpireDuration() {
		return DurationInMillis.ONE_HOUR;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(accountname, slug, state);
	}
}
