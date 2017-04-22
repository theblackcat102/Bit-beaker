package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.MainBranch;

/**
 * GET the repository's main branch using `repository` resource of `repositories` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/repository+Resource#repositoryResource-GETtherepository%27smainbranch">repository Resource - Bitbucket - Atlassian Documentation</a>
 */
public class MainBranchRequest extends BaseRepositoriesRequest<MainBranch> {

	/**
	 * Constructs a repository's main branch request.
	 *
	 * @param accountname The team or individual account
	 * @param slug A repository belonging to the account
	 */
	public MainBranchRequest(String accountname, String slug) {
		super(MainBranch.class, accountname, slug);
	}

	@Override
	public MainBranch loadDataFromNetwork() throws Exception {
		return getService().mainBranch(accountname, slug).loadDataFromNetwork();
	}

	@Override
	public String getCacheKey() {
		return "mainbranch" + Integer.valueOf(hashCode());
	}

	@Override
	public long getCacheExpireDuration() {
		return DurationInMillis.ONE_HOUR;
	}
}
