package fi.iki.kuitsi.bitbeaker.data.remote;

import android.support.annotation.Nullable;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import java.io.File;
import java.util.concurrent.TimeUnit;

import fi.iki.kuitsi.bitbeaker.data.api.interactor.ApiCall;

/**
 * A fluent API wrapper for {@linkplain ApiCall}s to execute them as {@linkplain SpiceRequest}s.
 */
class ApiCallCompiler<S> implements
		ApiCallCompilerStages.RApiCall<S>,
		ApiCallCompilerStages.RCacheExpireDuration,
		ApiCallCompilerStages.RCacheKey,
		ApiCallCompilerStages.RListener,
		ApiCallCompilerStages.RExecutable,
		ApiCallCompilerStages.RDownloadable {

	private final SpiceManager spiceManager;
	private ApiCall<S, ?> apiCall;
	private SpiceRequest request;
	private long cacheExpireDuration;
	@Nullable  private String cacheKeyPrefix;
	private RequestListener requestListener;

	protected ApiCallCompiler(SpiceManager spiceManager) {
		this.spiceManager = spiceManager;
	}

	public static <S> ApiCallCompilerStages.RApiCall<S> using(SpiceManager spiceManager) {
		return new ApiCallCompiler<>(spiceManager);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R> ApiCallCompilerStages.RListener<R> apiCall(ApiCall<S, R> apiCall) {
		this.apiCall = apiCall;
		return this;
	}

	@Override
	public ApiCallCompilerStages.RCacheExpireDuration listener(RequestListener listener) {
		request = ApiCallSpiceRequestDelegate.from(apiCall);
		this.requestListener = listener;
		return this;
	}

	@Override
	public ApiCallCompilerStages.RExecutable responseListener(RequestListener listener) {
		request = ApiResponseSpiceRequestDelegate.from(apiCall);
		this.requestListener = listener;
		return this;
	}

	@Override
	public ApiCallCompilerStages.RDownloadable downloadListener(RequestListener listener) {
		this.requestListener = listener;
		return this;
	}

	@Override
	public ApiCallCompilerStages.RExecutable alwaysExpired() {
		cacheExpireDuration = DurationInMillis.ALWAYS_EXPIRED;
		return this;
	}

	@Override
	public ApiCallCompilerStages.RCacheKey alwaysReturned() {
		cacheExpireDuration = DurationInMillis.ALWAYS_RETURNED;
		return this;
	}

	@Override
	public ApiCallCompilerStages.RCacheKey cacheExpiredOn(long duration, TimeUnit unit) {
		cacheExpireDuration = unit.toMillis(duration);
		return this;
	}

	@Override
	public ApiCallCompilerStages.RExecutable cacheKeyPrefix(String prefix) {
		cacheKeyPrefix = prefix;
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void execute() {
		String cacheKey = null;
		if (cacheKeyPrefix != null) {
			cacheKey = cacheKeyPrefix + Integer.toHexString(apiCall.hashCode());
		}
		final CachedSpiceRequest cachedRequest = new CachedSpiceRequest(request, cacheKey, cacheExpireDuration);
		spiceManager.execute(cachedRequest, requestListener);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void download(File file) {
		request = new ApiCallDownloadSpiceRequestDelegate(apiCall, file);
		spiceManager.execute(request, requestListener);
	}
}
