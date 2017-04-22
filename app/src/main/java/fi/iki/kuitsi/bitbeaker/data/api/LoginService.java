package fi.iki.kuitsi.bitbeaker.data.api;

import java.util.Map;

import fi.iki.kuitsi.bitbeaker.data.api.oauth.AccessTokenResponse;
import fi.iki.kuitsi.bitbeaker.domainobjects.User;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

import static fi.iki.kuitsi.bitbeaker.BuildConfig.CLIENT_CREDENTIALS;

public interface LoginService {

	@FormUrlEncoded
	@POST("access_token")
	@Headers({
			"Authorization: Basic " + CLIENT_CREDENTIALS,
			"Accept: application/json"
	})
	Call<AccessTokenResponse> accessToken(
			@FieldMap Map<String, String> fields);

	@GET
	Call<User> user(
			@Header("Authorization") String authorization,
			@Url String url);
}
