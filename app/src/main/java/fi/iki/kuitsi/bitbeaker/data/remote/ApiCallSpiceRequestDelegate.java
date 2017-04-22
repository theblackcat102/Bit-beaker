package fi.iki.kuitsi.bitbeaker.data.remote;

import com.octo.android.robospice.request.SpiceRequest;

import fi.iki.kuitsi.bitbeaker.data.api.interactor.ApiCall;
import fi.iki.kuitsi.bitbeaker.network.HttpException;
import retrofit2.Response;

final class ApiCallSpiceRequestDelegate<S, R> extends SpiceRequest<R> implements VisitableRequest<S> {

	private final ApiCall<S, R> delegate;
	private S service;

	ApiCallSpiceRequestDelegate(ApiCall<S, R> delegate) {
		super(delegate.responseType());
		this.delegate = delegate;
	}

	static <S, R> SpiceRequest<R> from(ApiCall<S, R> apiCall) {
		return new ApiCallSpiceRequestDelegate<>(apiCall);
	}

	@Override
	public R loadDataFromNetwork() throws Exception {
		Response<R> response = delegate.call(service).execute();
		if (response.isSuccessful()) {
			return response.body();
		}
		throw new HttpException(response);
	}

	@Override
	public void accept(RequestComponent<S> component) {
		this.service = component.getService();
		setRetryPolicy(component.retryPolicy());
	}
}
