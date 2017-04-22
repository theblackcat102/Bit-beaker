package fi.iki.kuitsi.bitbeaker.data.remote;

import java.io.IOException;

import fi.iki.kuitsi.bitbeaker.account.AuthenticatedUserManager;
import fi.iki.kuitsi.bitbeaker.data.api.oauth.AccessToken;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

final class AuthenticationInterceptor implements Interceptor {

	private final AuthenticatedUserManager userManager;

	public AuthenticationInterceptor(AuthenticatedUserManager userManager) {
		this.userManager = userManager;
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request original = chain.request();
		Request request;

		String token = userManager.getToken();
		if (!token.isEmpty()) {
			Request.Builder builder = original.newBuilder()
					.header(AccessToken.HEADER, token)
					.method(original.method(), original.body());
			request = builder.build();
		} else {
			request = original;
		}

		return chain.proceed(request);
	}
}
