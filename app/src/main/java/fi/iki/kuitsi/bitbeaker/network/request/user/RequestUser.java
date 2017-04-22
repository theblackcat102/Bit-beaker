package fi.iki.kuitsi.bitbeaker.network.request.user;

import com.octo.android.robospice.persistence.DurationInMillis;
import fi.iki.kuitsi.bitbeaker.network.request.BitbucketRequest;
import fi.iki.kuitsi.bitbeaker.network.response.user.UserEndpoint;

public class RequestUser extends BitbucketRequest<UserEndpoint> {

	public RequestUser() {
		super(UserEndpoint.class);
	}

	@Override
	public UserEndpoint loadDataFromNetwork() throws Exception {
		return getService().user_api_v1().loadDataFromNetwork();
	}

	@Override
	public String getCacheKey() {
		return "USER";
	}

	@Override
	public long getCacheExpireDuration() {
		return DurationInMillis.ALWAYS_RETURNED;
	}
}
