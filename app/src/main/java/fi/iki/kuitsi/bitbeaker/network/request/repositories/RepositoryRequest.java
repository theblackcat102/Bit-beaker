package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;

/**
 * GET a repository `repositories` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/repositories+Endpoint">repositories Endpoint - Bitbucket - Atlassian Documentation</a>
 */
public class RepositoryRequest extends BaseRepositoriesRequest<Repository> {

	/**
	 * Constructs a repository request.
	 *
	 * @param accountname The team or individual account
	 * @param slug A repository belonging to the account
	 */
	public RepositoryRequest(String accountname, String slug) {
		super(Repository.class, accountname, slug);
	}

	@Override
	public Repository loadDataFromNetwork() throws Exception {
		return getService().repository(accountname, slug).loadDataFromNetwork();
	}

	@Override
	public String getCacheKey() {
		return "repository" + Integer.valueOf(hashCode());
	}

	@Override
	public long getCacheExpireDuration() {
		return DurationInMillis.ONE_HOUR;
	}
}
