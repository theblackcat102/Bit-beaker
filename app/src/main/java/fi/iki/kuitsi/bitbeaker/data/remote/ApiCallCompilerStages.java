package fi.iki.kuitsi.bitbeaker.data.remote;

import android.support.annotation.CheckResult;

import com.octo.android.robospice.request.listener.RequestListener;

import java.io.File;
import java.util.concurrent.TimeUnit;

import fi.iki.kuitsi.bitbeaker.data.api.interactor.ApiCall;
import retrofit2.Response;

interface ApiCallCompilerStages {
	interface RApiCall<S> {
		@CheckResult <R> RListener<R> apiCall(ApiCall<S, R> apiCall);
	}

	interface RListener<R> {
		@CheckResult RCacheExpireDuration listener(RequestListener<R> listener);
		@CheckResult RExecutable responseListener(RequestListener<? extends Response<?>> listener);
		@CheckResult RDownloadable downloadListener(RequestListener<? extends File> listener);
	}

	interface RCacheExpireDuration {
		@CheckResult RExecutable alwaysExpired();
		@CheckResult RCacheKey alwaysReturned();
		@CheckResult RCacheKey cacheExpiredOn(long duration, TimeUnit unit);
	}

	interface RCacheKey {
		@CheckResult RExecutable cacheKeyPrefix(String prefix);
	}

	interface RExecutable {
		void execute();
	}

	interface RDownloadable {
		void download(File file);
	}
}
