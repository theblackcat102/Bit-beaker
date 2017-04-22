package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.RepositoryFollowers;
import fi.iki.kuitsi.bitbeaker.domainobjects.User;

import java.util.Collections;

public class RequestRepositoryFollowers extends BaseRepositoriesRequest<User.List> {

	public RequestRepositoryFollowers(String accountname, String slug) {
		super(User.List.class, accountname, slug);
	}

	@Override
	public User.List loadDataFromNetwork() throws Exception {
		RepositoryFollowers response = getService().repositoryFollowers(accountname, slug).loadDataFromNetwork();
		User.List followers = response.getFollowers();
		Collections.sort(followers);
		return response.getFollowers();
	}

	@Override
	public String getCacheKey() {
		return "repositoryfollowers+" + Integer.valueOf(hashCode());
	}

	@Override
	public long getCacheExpireDuration() {
		return DurationInMillis.ONE_HOUR;
	}
}
