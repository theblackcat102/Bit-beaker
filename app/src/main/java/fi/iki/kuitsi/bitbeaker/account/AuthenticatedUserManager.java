package fi.iki.kuitsi.bitbeaker.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.util.Log;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import fi.iki.kuitsi.bitbeaker.data.api.oauth.AccessToken;

public final class AuthenticatedUserManager {

	private static final String TAG = AuthenticatedUserManager.class.getSimpleName();
	private static final String TOKEN_TYPE = "token_type";

	private final AccountManager accountManager;
	private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	public AuthenticatedUserManager(AccountManager accountManager) {
		this.accountManager = accountManager;
		validateAccounts();
	}

	/**
	 * Create new {@link android.accounts.Account} or update the existing one.
	 * @param name User name
	 * @param accessToken Access token
	 * @param refreshToken Refresh token
	 */
	@UiThread
	public void onAccountAuthenticated(String name, AccessToken accessToken, String refreshToken) {
		Account existingAccount = getAccount();

		if (existingAccount != null) {
			removeAccount(existingAccount);
		}

		Bundle userData = new Bundle(1);
		userData.putString(TOKEN_TYPE, accessToken.tokenType());
		Account newAccount = new Account(name, Authenticator.getAccountType());
		readWriteLock.writeLock().lock();
		try {
			accountManager.addAccountExplicitly(newAccount, refreshToken, userData);
			accountManager.setAuthToken(newAccount, accessToken.tokenType(), accessToken.accessToken());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				accountManager.notifyAccountAuthenticated(newAccount);
			}
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	/**
	 * Get authenticated user account.
	 */
	public Account getAccount() {
		try {
			readWriteLock.readLock().lock();
			final Account[] accounts = accountManager.getAccountsByType(Authenticator.getAccountType());
			return accounts.length > 0 ? accounts[0] : null;
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	/**
	 * Get OAuth 2.0 token of the account.
	 */
	public String getToken() {
		Log.d(TAG, "getToken");
		try {
			readWriteLock.readLock().lock();
			final Account[] accounts = accountManager.getAccountsByType(Authenticator.getAccountType());
			if (accounts.length > 0) {
				Account account = accounts[0];
				String tokenType = accountManager.getUserData(account, TOKEN_TYPE);
				if (tokenType != null) {
					String token = accountManager.peekAuthToken(account, tokenType);
					AccessToken accessToken = new AccessToken(token, tokenType);
					return accessToken.string();
				}
			}
		} finally {
			readWriteLock.readLock().unlock();
		}
		return "";
	}

	public String getRefreshToken(Account account) {
		readWriteLock.readLock().lock();
		try {
			return accountManager.getPassword(account);
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	public void updateAccount(Account account, AccessToken accessToken, String refreshToken) {
		readWriteLock.writeLock().lock();
		try {
			accountManager.setUserData(account, TOKEN_TYPE, accessToken.tokenType());
			accountManager.setAuthToken(account, accessToken.tokenType(), accessToken.accessToken());
			accountManager.setPassword(account, refreshToken);
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public void invalidateToken(Account account) {
		readWriteLock.writeLock().lock();
		try {
			String tokenType = accountManager.getUserData(account, TOKEN_TYPE);
			if (tokenType != null) {
				accountManager.invalidateAuthToken(Authenticator.getAccountType(), tokenType);
			}
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	/**
	 * Check accounts' user data and remove.
	 */
	private void validateAccounts() {
		Log.d(TAG, "validateAccounts");
		try {
			readWriteLock.writeLock().lock();
			final Account[] accounts = accountManager.getAccountsByType(Authenticator.getAccountType());
			for (Account account : accounts) {
				if (accountManager.getUserData(account, TOKEN_TYPE) == null) {
					Log.d(TAG, String.format("remove '%s' account without token type", account.name));
					removeAccount(account);
				}
			}
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	/**
	 * Removes the account from the AccountManager.
	 */
	private void removeAccount(Account account) {
		Log.d(TAG, String.format("remove '%s' account", account.name));
		readWriteLock.writeLock().lock();
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
				accountManager.removeAccountExplicitly(account);
			} else {
				//noinspection deprecation
				accountManager.removeAccount(account, null, null);
			}
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}
}
