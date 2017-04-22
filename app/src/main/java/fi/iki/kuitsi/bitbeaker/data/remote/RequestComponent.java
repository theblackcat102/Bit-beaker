package fi.iki.kuitsi.bitbeaker.data.remote;

import com.octo.android.robospice.retry.RetryPolicy;

public interface RequestComponent<S> extends ServiceProvider<S> {
	S getService();
	RetryPolicy retryPolicy();
}
