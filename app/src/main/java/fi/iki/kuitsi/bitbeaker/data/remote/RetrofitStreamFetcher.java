package fi.iki.kuitsi.bitbeaker.data.remote;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.util.ContentLengthInputStream;

import java.io.IOException;
import java.io.InputStream;

import fi.iki.kuitsi.bitbeaker.data.api.interactor.ApiCall;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Fetches an {@link InputStream} using Retrofit.
 */
final class RetrofitStreamFetcher<S> implements DataFetcher<InputStream> {

	private final S service;
	private final ApiCall<S, ResponseBody> apiCall;
	private volatile Call<ResponseBody> call;
	private ResponseBody responseBody;
	private InputStream stream;

	public RetrofitStreamFetcher(S service, ApiCall<S, ResponseBody> apiCall) {
		this.service = service;
		this.apiCall = apiCall;
	}

	@Override
	public InputStream loadData(Priority priority) throws Exception {
		call = apiCall.call(service);
		Response<ResponseBody> response = call.execute();
		responseBody = response.body();
		if (!response.isSuccessful()) {
			throw new IOException("Request failed using code: " + response.code());
		}

		long contentLength = responseBody.contentLength();
		stream = ContentLengthInputStream.obtain(responseBody.byteStream(), contentLength);
		return stream;
	}

	@Override
	public void cleanup() {
		try {
			if (stream != null) {
				stream.close();
			}
		} catch (IOException e) {
			// Ignored
		}
		if (responseBody != null) {
			responseBody.close();
		}
	}

	@Override
	public String getId() {
		return apiCall.getClass().getSimpleName() + Integer.toHexString(apiCall.hashCode());
	}

	@Override
	public void cancel() {
		Call local = call;
		if (local != null) {
			local.cancel();
		}
	}
}
