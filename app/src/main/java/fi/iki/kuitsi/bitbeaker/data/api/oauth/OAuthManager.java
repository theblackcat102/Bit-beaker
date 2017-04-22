package fi.iki.kuitsi.bitbeaker.data.api.oauth;

import android.net.Uri;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import static fi.iki.kuitsi.bitbeaker.BuildConfig.API_KEY;
import static fi.iki.kuitsi.bitbeaker.BuildConfig.CALLBACK_HOST;
import static fi.iki.kuitsi.bitbeaker.BuildConfig.CALLBACK_SCHEME;

public final class OAuthManager {

	private static final Uri CALLBACK_URI = new Uri.Builder().scheme(CALLBACK_SCHEME).authority(CALLBACK_HOST).build();

	private OAuthManager() { }

	public static Uri authorizationUri() {
		return new Uri.Builder()
				.scheme("https")
				.authority("bitbucket.org")
				.appendPath("site")
				.appendPath("oauth2")
				.appendPath("authorize")
				.appendQueryParameter("client_id", API_KEY)
				.appendQueryParameter("response_type", "code")
				.build();
	}

	public static boolean checkUri(@Nullable Uri uri) {
		return uri != null && CALLBACK_URI.getScheme().equals(uri.getScheme())
				&& CALLBACK_URI.getHost().equals(uri.getHost())
				&& uri.getQueryParameter("code") != null;
	}

	public static Map<String, String> accessTokenRequest(String code) {
		Map<String, String> map = new HashMap<>(2);
		map.put("grant_type", "authorization_code");
		map.put("code", code);
		return map;
	}

	public static Map<String, String> refreshTokenRequest(String refreshToken) {
		Map<String, String> map = new HashMap<>(2);
		map.put("grant_type", "refresh_token");
		map.put("refresh_token", refreshToken);
		return map;
	}
}
