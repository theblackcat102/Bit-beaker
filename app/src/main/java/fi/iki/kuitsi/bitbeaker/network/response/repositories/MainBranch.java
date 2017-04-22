package fi.iki.kuitsi.bitbeaker.network.response.repositories;

/**
 * Response of GET the repository's main branch
 * using `repository` resource of `repositories` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/repository+Resource#repositoryResource-GETtherepository%27smainbranch">repository Resource - Bitbucket - Atlassian Documentation</a>
 */
public class MainBranch {
	/**
	 * The name of repository's main branch.
	 */
	public String name;
}
