package fi.iki.kuitsi.bitbeaker.account;

import android.accounts.AccountManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/**
 * A Bound Service that instantiates the authenticator when started. When the system binds to this
 * Service to make the IPC call return the authenticator's IBinder.
 */
public class AuthenticatorService extends Service {

	private Authenticator authenticator;

	@Override
	public void onCreate() {
		super.onCreate();
		// Create a new authenticator object
		authenticator = new Authenticator(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (AccountManager.ACTION_AUTHENTICATOR_INTENT.equals(intent.getAction())) {
			return authenticator.getIBinder();
		} else {
			return null;
		}
	}
}
