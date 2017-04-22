package fi.iki.kuitsi.bitbeaker.data.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

final class SpiceCallAdapter implements CallAdapter<SpiceCall<?>> {

	static final Factory FACTORY = new Factory() {
		@Override
		public CallAdapter<?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
			if (getRawType(returnType) != SpiceCall.class) {
				return null;
			}
			Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);
			return new SpiceCallAdapter(responseType);
		}

		@Override
		public String toString() {
			return "SpiceCall";
		}
	};

	private final Type responseType;

	SpiceCallAdapter(Type responseType) {
		this.responseType = responseType;
	}

	@Override
	public Type responseType() {
		return responseType;
	}

	@Override
	public <R> SpiceCall<R> adapt(Call<R> call) {
		return new SpiceCallImpl<>(call);
	}
}
