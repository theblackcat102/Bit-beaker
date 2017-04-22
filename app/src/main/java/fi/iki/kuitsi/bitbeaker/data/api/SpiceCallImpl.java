package fi.iki.kuitsi.bitbeaker.data.api;

import fi.iki.kuitsi.bitbeaker.network.HttpException;
import retrofit2.Call;
import retrofit2.Response;

final class SpiceCallImpl<T> implements SpiceCall<T> {

	private final Call<T> networkCall;

	SpiceCallImpl(Call<T> networkCall) {
		this.networkCall = networkCall;
	}

	@Override
	public T loadDataFromNetwork() throws Exception {
		Response<T> response = networkCall.execute();
		if (response.isSuccessful()) {
			return response.body();
		}
		throw new HttpException(response);
	}
}
