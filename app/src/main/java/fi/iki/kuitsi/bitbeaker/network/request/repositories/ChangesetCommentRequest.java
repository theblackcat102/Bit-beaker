package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.domainobjects.ChangesetComment;
import fi.iki.kuitsi.bitbeaker.util.Objects;

import java.util.Iterator;

/**
 * GET a list of comments on a changeset using `changesets` resource of `repositories` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/changesets+Resource
 * #changesetsResource-GETalistofcommentsonachangeset">
 * changesets Resource - Bitbucket - Atlassian Documentation</a>
 */
public class ChangesetCommentRequest extends BaseRepositoriesRequest<ChangesetComment.List> {

	/**
	 * The node changeset identifier.
	 */
	private final String node;

	/**
	 * Constructs a changeset list request.
	 *
	 * @param accountname The team or individual account owning the repo
	 * @param slug The repo identifier
	 * @param node The node changeset identifier
	 */
	public ChangesetCommentRequest(String accountname, String slug, String node) {
		super(ChangesetComment.List.class, accountname, slug);
		this.node = node;
	}

	@Override
	public ChangesetComment.List loadDataFromNetwork() throws Exception {
		ChangesetComment.List comments = getService().changesetComment(accountname, slug, node).loadDataFromNetwork();
		Iterator<ChangesetComment> iterator = comments.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().isDeleted()) {
				iterator.remove();
			}
		}
		return comments;
	}

	@Override
	public String getCacheKey() {
		return "changesetcomment" + String.valueOf(hashCode());
	}

	@Override
	public long getCacheExpireDuration() {
		return 15L * DurationInMillis.ONE_MINUTE;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(accountname, slug, node);
	}
}
