package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.domainobjects.Changeset;
import fi.iki.kuitsi.bitbeaker.util.Objects;

/**
 * Gets a specific changeset node using `changesets` resource of `repositories` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/changesets+Resource#changesetsResource-GETanindividualchangeset>changesets Resource - Bitbucket - Atlassian Documentation</a>
 */
public class ChangesetRequest extends BaseRepositoriesRequest<Changeset> {

	private final String node;

	public ChangesetRequest(String accountname, String slug, String node) {
		super(Changeset.class, accountname, slug);
		this.node = node;
	}

	@Override
	public Changeset loadDataFromNetwork() throws Exception {
		return getService().changeset(accountname, slug, node).loadDataFromNetwork();
	}

	@Override
	public String getCacheKey() {
		return "changeset" + String.valueOf(hashCode());
	}

	@Override
	public long getCacheExpireDuration() {
		return DurationInMillis.ONE_WEEK;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(accountname, slug, node);
	}
}
