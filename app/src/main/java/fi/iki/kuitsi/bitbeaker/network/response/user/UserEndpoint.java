package fi.iki.kuitsi.bitbeaker.network.response.user;

import fi.iki.kuitsi.bitbeaker.domainobjects.User;

public class UserEndpoint {

	private User user;

	public UserEndpoint user(User user) {
		this.user = user;
		return this;
	}

	public User getUser() {
		return user;
	}
}
