package fi.iki.kuitsi.bitbeaker.data.remote;

import com.google.gson.reflect.TypeToken;
import com.octo.android.robospice.request.SpiceRequest;

import fi.iki.kuitsi.bitbeaker.data.api.interactor.ApiCall;
import retrofit2.Response;

final class ApiResponseSpiceRequestDelegate<S, R> extends SpiceRequest<Response<R>> implements VisitableRequest<S> {

	private ApiCall<S, R> apiCall;
	private S service;

	ApiResponseSpiceRequestDelegate(Class<Response<R>> clazz, ApiCall<S, R> apiCall) {
		super(clazz);
		this.apiCall = apiCall;
	}

	static <S, R> SpiceRequest<Response<R>> from(ApiCall<S, R> apiCall) {
		Class<Response<R>> clazz = (Class<Response<R>>) new TypeToken<Response<R>>() { }.getRawType();
		return new ApiResponseSpiceRequestDelegate<>(clazz, apiCall);
	}

	@Override
	public Response<R> loadDataFromNetwork() throws Exception {
		return apiCall.call(service).execute();
	}

	@Override
	public void accept(RequestComponent<S> component) {
		this.service = component.getService();
		setRetryPolicy(component.retryPolicy());
	}
}
