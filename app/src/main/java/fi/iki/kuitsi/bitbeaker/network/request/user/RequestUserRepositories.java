package fi.iki.kuitsi.bitbeaker.network.request.user;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;
import fi.iki.kuitsi.bitbeaker.network.request.BitbucketRequest;

public class RequestUserRepositories extends BitbucketRequest<Repository.List> {

	public RequestUserRepositories() {
		super(Repository.List.class);
	}

	@Override
	public Repository.List loadDataFromNetwork() throws Exception {
		return getService().userRepositories().loadDataFromNetwork();
	}

	@Override
	public String getCacheKey() {
		return "repositories";
	}

	@Override
	public long getCacheExpireDuration() {
		return DurationInMillis.ONE_HOUR;
	}
}
