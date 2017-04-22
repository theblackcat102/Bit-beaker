package fi.iki.kuitsi.bitbeaker.data.remote;

import android.accounts.Account;
import android.util.Log;

import java.io.IOException;
import java.util.Map;

import fi.iki.kuitsi.bitbeaker.account.AuthenticatedUserManager;
import fi.iki.kuitsi.bitbeaker.data.api.LoginService;
import fi.iki.kuitsi.bitbeaker.data.api.oauth.AccessToken;
import fi.iki.kuitsi.bitbeaker.data.api.oauth.OAuthManager;
import fi.iki.kuitsi.bitbeaker.data.api.oauth.AccessTokenResponse;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;


final class RefreshTokenAuthenticator implements Authenticator {

	private final LoginService loginService;
	private final AuthenticatedUserManager userManager;

	public RefreshTokenAuthenticator(LoginService loginService, AuthenticatedUserManager userManager) {
		this.loginService = loginService;
		this.userManager = userManager;
	}

	@Override
	public Request authenticate(Route route, Response response) throws IOException {
		// If we’ve failed 2 times, give up.
		if (responseCount(response) >= 2) {
			return null;
		}

		Request request = response.request();
		if (request.header(AccessToken.HEADER) == null) {
			return null;
		}

		Account account = userManager.getAccount();
		if (account == null) {
			return null;
		}

		AccessToken accessToken = refreshToken(account);

		if (accessToken != null) {
			// repeat request with updated access token
			return request.newBuilder()
					.header(AccessToken.HEADER, accessToken.string())
					.build();
		}

		return request;
	}

	private int responseCount(Response response) {
		int result = 1;

		while ((response = response.priorResponse()) != null) {
			result++;
		}

		return result;
	}

	private AccessToken refreshToken(Account account) throws IOException {
		String refreshToken = userManager.getRefreshToken(account);
		Map<String, String> request = OAuthManager.refreshTokenRequest(refreshToken);
		retrofit2.Response<AccessTokenResponse> response = loginService.accessToken(request).execute();
		if (response.isSuccessful()) {
			AccessTokenResponse accessTokenResponse = response.body();
			userManager.updateAccount(account, accessTokenResponse.accessToken(), accessTokenResponse.refreshToken());
			return accessTokenResponse.accessToken();
		} else {
			userManager.invalidateToken(account);
			Log.e("refresh token auth.", "refresh failed. code=" + response.code() + " " + response.errorBody().string());
		}
		return null;
	}
}
