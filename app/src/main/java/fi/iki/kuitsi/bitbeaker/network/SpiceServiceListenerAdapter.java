package fi.iki.kuitsi.bitbeaker.network;

import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.SpiceServiceListener;

/**
 * Adapter for {@link SpiceServiceListener} interface.
 */
public class SpiceServiceListenerAdapter implements SpiceServiceListener {
	@Override
	public void onRequestSucceeded(CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext) {
		onIdle();
	}

	@Override
	public void onRequestFailed(CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext) {
		onIdle();
	}

	@Override
	public void onRequestCancelled(CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext) {
		onIdle();
	}

	@Override
	public void onRequestProgressUpdated(CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext) {

	}

	@Override
	public void onRequestAdded(CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext) {
		onActive();
	}

	@Override
	public void onRequestAggregated(CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext) {

	}

	@Override
	public void onRequestNotFound(CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext) {

	}

	@Override
	public void onRequestProcessed(CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext) {

	}

	@Override
	public void onServiceStopped() {

	}

	/**
	 * Service started some background active.
	 */
	protected void onActive() {
	}

	/**
	 * Service returned to idle state.
	 */
	protected void onIdle() {
	}
}
