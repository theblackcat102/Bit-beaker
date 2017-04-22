package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.BranchNames;

/**
 * GET a list of branches associated with a repository using `repository` resource of `repositories` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/repository+Resource#repositoryResource-GETlistofbranches">repository Resource - Bitbucket - Atlassian Documentation</a>
 */
public class BranchListRequest extends BaseRepositoriesRequest<BranchNames> {

	/**
	 * Constructs a branch list request.
	 *
	 * @param accountname The team or individual account
	 * @param slug A repository belonging to the account
	 */
	public BranchListRequest(String accountname, String slug) {
		super(BranchNames.class, accountname, slug);
	}

	@Override
	public BranchNames loadDataFromNetwork() throws Exception {
		return getService().branches(accountname, slug).loadDataFromNetwork();
	}

	@Override
	public String getCacheKey() {
		return "branchlist" + Integer.valueOf(hashCode());
	}

	@Override
	public long getCacheExpireDuration() {
		return DurationInMillis.ONE_HOUR;
	}
}
