package fi.iki.kuitsi.bitbeaker.network.request;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.SpiceRequest;

import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;
import fi.iki.kuitsi.bitbeaker.data.remote.RequestComponent;
import fi.iki.kuitsi.bitbeaker.data.remote.VisitableRequest;

/**
 * Common base class of Bitbucket API requests.
 *
 * @param <RESULT> The result type of this request
 */
public abstract class BitbucketRequest<RESULT> extends SpiceRequest<RESULT>
		implements VisitableRequest<BitbucketService> {

	private BitbucketService service;

	public BitbucketRequest(Class<RESULT> clazz) {
		super(clazz);
	}

	@Override
	public final void accept(RequestComponent<BitbucketService> component) {
		setRetryPolicy(component.retryPolicy());
		service = component.getService();
	}

	/**
	 * Get the key used to store and retrieve the result of the request in the cache.
	 * <p/>
	 * Recommended key value structure: request identifier (unique name) + hash
	 *
	 * @return The key value.
	 */
	public String getCacheKey() {
		return null;
	}

	public int hashCode() {
		return 0;
	}

	/**
	 * Get the duration in milliseconds after which the cached value of the request will be
	 * considered to be expired.
	 *
	 * @return The cache expire duration in milliseconds.
	 */
	public long getCacheExpireDuration() {
		return DurationInMillis.ALWAYS_EXPIRED;
	}

	protected final BitbucketService getService() {
		return service;
	}
}
