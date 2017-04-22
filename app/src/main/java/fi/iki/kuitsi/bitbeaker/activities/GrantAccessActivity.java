package fi.iki.kuitsi.bitbeaker.activities;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Map;

import fi.iki.kuitsi.bitbeaker.AppComponent;
import fi.iki.kuitsi.bitbeaker.AppComponentService;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.account.AuthenticatedUserManager;
import fi.iki.kuitsi.bitbeaker.account.Authenticator;
import fi.iki.kuitsi.bitbeaker.data.api.LoginService;
import fi.iki.kuitsi.bitbeaker.data.api.oauth.AccessToken;
import fi.iki.kuitsi.bitbeaker.data.api.oauth.OAuthManager;
import fi.iki.kuitsi.bitbeaker.data.api.oauth.AccessTokenResponse;
import fi.iki.kuitsi.bitbeaker.domainobjects.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GrantAccessActivity extends BaseActivity {

	private static final String TAG = GrantAccessActivity.class.getSimpleName();

	private AuthenticatedUserManager userManager;
	private LoginService loginService;
	@Nullable private AccountAuthenticatorResponse authenticatorResponse;
	@Nullable private Intent resultIntent;

	public GrantAccessActivity() {
		super(R.layout.grant_access);
	}

	static Intent createResultIntent(User user, AccessTokenResponse response) {
		Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, user.getUsername());
		intent.putExtra(AccountManager.KEY_PASSWORD, response.refreshToken());
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Authenticator.getAccountType());
		intent.putExtra(AccountManager.KEY_AUTHTOKEN, response.accessToken().accessToken());
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppComponent appComponent = AppComponentService.obtain(getApplicationContext());
		userManager = appComponent.userManager();
		loginService = appComponent.loginService();

		authenticatorResponse = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
		if (authenticatorResponse != null) {
			authenticatorResponse.onRequestContinued();
		}
		resultIntent = null;

		startActivity(new Intent(Intent.ACTION_VIEW, OAuthManager.authorizationUri())
				.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Uri data = intent.getData();
		if (OAuthManager.checkUri(data)) {
			requestAccessToken(data.getQueryParameter("code"));
		}
	}

	@Override
	public void finish() {
		if (resultIntent != null) {
			setResult(RESULT_OK, resultIntent);
			if (authenticatorResponse != null) {
				authenticatorResponse.onResult(resultIntent.getExtras());
			}
		} else {
			setResult(RESULT_CANCELED);
			if (authenticatorResponse != null) {
				authenticatorResponse.onError(RESULT_CANCELED, "");
			}
		}
		super.finish();
	}

	private void requestAccessToken(String code) {
		Log.d(TAG, "get access token");
		final Map<String, String> request = OAuthManager.accessTokenRequest(code);
		loginService.accessToken(request).enqueue(new Callback<AccessTokenResponse>() {
			@Override
			public void onResponse(Call<AccessTokenResponse> call, Response<AccessTokenResponse> response) {
				if (response.isSuccessful()) {
					final AccessTokenResponse accessTokenResponse = response.body();
					final AccessToken accessToken = accessTokenResponse.accessToken();
					Log.d(TAG, "get user info");
					loginService.user(accessToken.string(), "https://api.bitbucket.org/2.0/user").enqueue(new Callback<User>() {
						@Override
						public void onResponse(Call<User> call, Response<User> response) {
							Log.v(TAG, "onResponse" + response.headers());
							if (response.isSuccessful()) {
								User user = response.body();
								userManager.onAccountAuthenticated(user.getUsername(),
										accessToken,
										accessTokenResponse.refreshToken());
								if (authenticatorResponse != null) {
									resultIntent = createResultIntent(user, accessTokenResponse);
								}
								finish();
							} else {
								Log.e(TAG, "failed to get user " + response.code());
								finish();
							}
						}

						@Override
						public void onFailure(Call<User> call, Throwable t) {
							Log.e(TAG, "failed to get user", t);
							finish();
						}
					});
				} else {
					Log.e(TAG, "failed to get access token " + response.code());
					finish();
				}
			}

			@Override
			public void onFailure(Call<AccessTokenResponse> call, Throwable t) {
				Log.e(TAG, "failed to get access token", t);
				finish();
			}
		});
	}
}
