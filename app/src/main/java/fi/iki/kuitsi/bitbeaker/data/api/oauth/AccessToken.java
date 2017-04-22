package fi.iki.kuitsi.bitbeaker.data.api.oauth;

public final class AccessToken {
	public static final String HEADER = "Authorization";

	private final String access_token;
	private final String token_type;

	public AccessToken(String accessToken, String tokenType) {
		this.access_token = accessToken;
		this.token_type = tokenType;
	}

	public String accessToken() {
		return access_token;
	}

	public String tokenType() {
		return token_type;
	}

	public boolean isEmpty() {
		return access_token == null || access_token.isEmpty() || token_type == null || token_type.isEmpty();
	}

	public String string() {
		return token_type.substring(0, 1).toUpperCase()
				+ token_type.substring(1)
				+ ' '
				+ access_token;
	}
}
