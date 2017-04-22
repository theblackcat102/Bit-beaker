package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.domainobjects.IssueComment;
import fi.iki.kuitsi.bitbeaker.util.Objects;

/**
 * Get the comments for an issue using `issues` resource of `repositories` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/issues+Resource#issuesResource-GETthecommentsforanissue">issues Resource - Bitbucket - Atlassian Documentation</a>
 */
public class IssueCommentsRequest extends BaseRepositoriesRequest<IssueComment.List> {

	/** The issue identifier. */
	private final int issueId;

	/**
	 * Constructs an issue comments request.
	 *
	 * @param accountname The team or individual account owning the repository
	 * @param slug The repository identifier
	 * @param issueId The issue identifier
	 */
	public IssueCommentsRequest(String accountname, String slug, int issueId) {
		super(IssueComment.List.class, accountname, slug);
		this.issueId = issueId;
	}

	@Override
	public String getCacheKey() {
		return "issuecomment" + String.valueOf(hashCode());
	}

	@Override
	public long getCacheExpireDuration() {
		return DurationInMillis.ALWAYS_EXPIRED;
	}

	@Override
	public IssueComment.List loadDataFromNetwork() throws Exception {
		return getService().issueComments(accountname, slug, issueId).loadDataFromNetwork();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(accountname, slug, issueId);
	}
}
