package fi.iki.kuitsi.bitbeaker.data.remote;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.stream.StreamModelLoader;

import java.io.InputStream;

import fi.iki.kuitsi.bitbeaker.data.api.interactor.ApiCall;
import okhttp3.ResponseBody;

/**
 * A model loader for fetching media using Retrofit.
 */
final class RetrofitUrlLoader<S> implements StreamModelLoader<ApiCall<S, ResponseBody>> {

	private final S service;

	public RetrofitUrlLoader(S service) {
		this.service = service;
	}

	@Override
	public DataFetcher<InputStream> getResourceFetcher(ApiCall<S, ResponseBody> apiCall,
			int width, int height) {
		return new RetrofitStreamFetcher<>(service, apiCall);
	}
}
