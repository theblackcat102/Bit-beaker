package fi.iki.kuitsi.bitbeaker.data.remote;

import com.octo.android.robospice.exception.NetworkException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.retry.DefaultRetryPolicy;
import com.octo.android.robospice.retry.RetryPolicy;

import fi.iki.kuitsi.bitbeaker.network.HttpException;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

/**
 * Customized retry policy based on {@link DefaultRetryPolicy}.
 * Stop retrying in case of Http error.
 */
class BitbucketRetryPolicy implements RetryPolicy {

	/** The default number of retry attempts.*/
	private static final int RETRY_COUNT = 3;

	/** The default delay before retry a request (in ms). */
	private static final long DELAY_BEFORE_RETRY = 1000;

	/** The default backoff multiplier. */
	private static final float BACKOFF_MULT = 1.25f;

	/** The number of retry attempts. */
	private int retryCount = RETRY_COUNT;

	/**
	 * The delay to wait before next retry attempt. Will be multiplied by
	 * {@link #BACKOFF_MULT} between every retry attempt.
	 */
	private long delayBeforeRetry = DELAY_BEFORE_RETRY;

	@Override
	public int getRetryCount() {
		return retryCount;
	}

	@Override
	public void retry(SpiceException e) {
		if (doNotRetry(e)) {
			retryCount = 0;
		} else {
			retryCount--;
			delayBeforeRetry = (long) (delayBeforeRetry * BACKOFF_MULT);
		}
	}

	@Override
	public long getDelayBeforeRetry() {
		return delayBeforeRetry;
	}

	private static boolean doNotRetry(SpiceException spiceException) {
		if (spiceException instanceof NetworkException) {
			Throwable networkException = spiceException.getCause();
			if (networkException instanceof HttpException) {
				HttpException httpException = (HttpException) networkException;
				int code = httpException.code();
				return checkStatus(code);
			}
		}
		return false;
	}

	private static boolean checkStatus(int status) {
		switch (status) {
			/*
			 * Returned if the caller submits a badly formed request.
			 * For example, the caller can receive this return if you forget a required parameter.
			 */
			case HTTP_BAD_REQUEST:
			/*
			 * Returned if the call requires authentication and either the credentials
			 * provided failed or no credentials were provided.
			 */
			case HTTP_UNAUTHORIZED:
			/*
			 * Returned if the caller attempts to make a call or modify a resource for which
			 * the caller is not authorized. The request was a valid request, the caller's
			 * authentication credentials succeeded but those credentials do not grant the
			 * caller permission to access the resource.
			 */
			case HTTP_FORBIDDEN:
			/*
			 * Returned if the specified resource does not exist.
			 */
			case HTTP_NOT_FOUND:
				return true;
			default:
				return false;
		}
	}
}
