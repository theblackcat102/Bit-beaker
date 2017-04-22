package fi.iki.kuitsi.bitbeaker.network.response.repositories;

import fi.iki.kuitsi.bitbeaker.domainobjects.User;

/**
 * Response of GET the repository followers
 * using `followers` resource of `repositories` endpoint.
 */
public class RepositoryFollowers {
	private int count;
	private User.List followers;

	public User.List getFollowers() {
		return followers;
	}
}
