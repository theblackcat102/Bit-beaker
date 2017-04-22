package fi.iki.kuitsi.bitbeaker.data.remote;

import com.octo.android.robospice.request.SpiceRequest;

import java.io.File;

import fi.iki.kuitsi.bitbeaker.data.api.interactor.ApiCall;
import fi.iki.kuitsi.bitbeaker.network.HttpException;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Response;

final class ApiCallDownloadSpiceRequestDelegate<S> extends SpiceRequest<File> implements VisitableRequest<S> {
	private final ApiCall<S, ResponseBody> apiCall;
	private final File file;
	private S service;

	ApiCallDownloadSpiceRequestDelegate(ApiCall<S, ResponseBody> delegate, File file) {
		super(File.class);
		this.file = file;
		this.apiCall = delegate;
	}

	@Override
	public File loadDataFromNetwork() throws Exception {
		Response<ResponseBody> response = apiCall.call(service).execute();
		if (response.isSuccessful()) {
			BufferedSink sink = Okio.buffer(Okio.sink(file));
			sink.writeAll(response.body().source());
			return file;
		}
		throw new HttpException(response);
	}

	@Override
	public void accept(RequestComponent<S> component) {
		this.service = component.getService();
		setRetryPolicy(component.retryPolicy());
	}
}
