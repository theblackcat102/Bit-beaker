package fi.iki.kuitsi.bitbeaker.network.response.users;

import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;
import fi.iki.kuitsi.bitbeaker.domainobjects.User;

/**
 * Response of GET the account profile
 * using `account` resource of `users` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/account+Resource#accountResource-GETtheaccountprofile">account Resource - Bitbucket - Atlassian Documentation</a>
 */
public class AccountProfile {
	private final User user;
	private final Repository.List repositories;

	public AccountProfile(User user, Repository.List repositories) {
		this.user = user;
		this.repositories = repositories;
	}

	public User getUser() {
		return user;
	}

	public Repository.List getRepositories() {
		return repositories;
	}
}
