package fi.iki.kuitsi.bitbeaker.data.api.interactor;

import retrofit2.Call;

/**
 * An invocation of an API method that sends a request to Bitbucket server and returns a response.
 *
 * @param <S> Service type.
 * @param <R> Successful response type.
 */
public interface ApiCall<S, R> {
	Class<R> responseType();
	Call<R> call(S service);
}
