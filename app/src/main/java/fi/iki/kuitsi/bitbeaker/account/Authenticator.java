package fi.iki.kuitsi.bitbeaker.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import fi.iki.kuitsi.bitbeaker.activities.GrantAccessActivity;

import static fi.iki.kuitsi.bitbeaker.BuildConfig.APPLICATION_ID;

/**
 * Implement AbstractAccountAuthenticator and stub out all of its methods.
 * <a href="http://developer.android.com/training/sync-adapters/creating-authenticator.html">Creating a Stub Authenticator</a>
 *
 * Overridden methods are not implemented because the app has no
 * {@link android.accounts.AccountAuthenticatorActivity}.
 *
 * The SyncService requires an Account.
 */
public class Authenticator extends AbstractAccountAuthenticator {

	private static final String ACCOUNT_TYPE = APPLICATION_ID;

	private final Context context;

	public Authenticator(Context context) {
		super(context);
		this.context = context.getApplicationContext();
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
			String authTokenType, String[] requiredFeatures, Bundle options)
			throws NetworkErrorException {
		Bundle result = new Bundle();
		result.putParcelable(AccountManager.KEY_INTENT,
				new Intent(context, GrantAccessActivity.class)
						.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response));
		return result;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
			Bundle options) throws NetworkErrorException {
		// Ignore attempts to confirm credentials
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
			String authTokenType, Bundle options) throws NetworkErrorException {
		// Getting an authentication token is not supported
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		// Getting a label for the auth token is not supported
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
			String[] features) throws NetworkErrorException {
		// Checking features for the account is not supported
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
			String authTokenType, Bundle options) throws NetworkErrorException {
		// Updating user credentials is not supported
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
		// Editing properties is not supported
		throw new UnsupportedOperationException();
	}

	public static String getAccountType() {
		return ACCOUNT_TYPE;
	}
}
