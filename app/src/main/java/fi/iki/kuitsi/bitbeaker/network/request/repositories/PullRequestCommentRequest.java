package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.domainobjects.PullRequestComment;
import fi.iki.kuitsi.bitbeaker.util.Objects;

/**
 * GET a list of a pull request comments using `pullrequests` resource of `repositories` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/pullrequests+Resource#pullrequestsResource-GETalistofapullrequestcomments">pullrequests Resource - Bitbucket - Atlassian Documentation</a>
 */
public class PullRequestCommentRequest extends BaseRepositoriesRequest<PullRequestComment.List> {

	/**
	 * The pull request id.
	 */
	private int id;

	/**
	 * Constructs a comment list request associated with the specified pull request.
	 *
	 * @param accountname The team or individual account
	 * @param slug A repository belonging to the account
	 * @param id The pull request id
	 */
	public PullRequestCommentRequest(String accountname, String slug, int id) {
		super(PullRequestComment.List.class, accountname, slug);
		this.id = id;
	}

	@Override
	public PullRequestComment.List loadDataFromNetwork() throws Exception {
		return getService().pullRequestComments(accountname, slug, id).loadDataFromNetwork();
	}

	@Override
	public String getCacheKey() {
		return "pullrequestcomments" + String.valueOf(hashCode());
	}

	@Override
	public long getCacheExpireDuration() {
		return 15 * DurationInMillis.ONE_MINUTE;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(accountname, slug, id);
	}
}
