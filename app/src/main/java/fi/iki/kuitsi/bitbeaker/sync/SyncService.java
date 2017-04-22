package fi.iki.kuitsi.bitbeaker.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Define a Service that returns an IBinder for the {@link SyncAdapter} class, allowing the sync
 * framework to call {@link SyncAdapter#onPerformSync}.
 */
public class SyncService extends Service {

	/** Storage for an instance of the sync adapter. */
	private static SyncAdapter syncAdapter = null;

	/** Object to use as a thread-safe lock. */
	private static final Object LOCK = new Object();

	/**
	 * Instantiate the sync adapter object.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		synchronized (LOCK) {
			if (syncAdapter == null) {
				syncAdapter = new SyncAdapter(getApplicationContext(), true);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return syncAdapter.getSyncAdapterBinder();
	}
}
