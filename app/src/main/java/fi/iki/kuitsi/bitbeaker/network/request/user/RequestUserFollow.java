package fi.iki.kuitsi.bitbeaker.network.request.user;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;
import fi.iki.kuitsi.bitbeaker.network.request.BitbucketRequest;

public class RequestUserFollow extends BitbucketRequest<Repository.List> {

	public RequestUserFollow() {
		super(Repository.List.class);
	}

	@Override
	public Repository.List loadDataFromNetwork() throws Exception {
		return getService().userFollow().loadDataFromNetwork();
	}

	@Override
	public String getCacheKey() {
		return "follow";
	}

	@Override
	public long getCacheExpireDuration() {
		return DurationInMillis.ONE_HOUR;
	}
}
