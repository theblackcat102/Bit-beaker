package fi.iki.kuitsi.bitbeaker.data.api.oauth;

public final class AccessTokenResponse {
	private final String access_token;
	private final String token_type;
	private final String refresh_token;

	public AccessTokenResponse(String accessToken, String tokenType, String refreshToken) {
		this.access_token = accessToken;
		this.token_type = tokenType;
		this.refresh_token = refreshToken;
	}

	public AccessToken accessToken() {
		return new AccessToken(access_token, token_type);
	}

	public String refreshToken() {
		return refresh_token;
	}
}
