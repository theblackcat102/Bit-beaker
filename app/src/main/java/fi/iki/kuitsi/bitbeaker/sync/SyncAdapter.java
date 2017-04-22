package fi.iki.kuitsi.bitbeaker.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import fi.iki.kuitsi.bitbeaker.AppComponentService;

/**
 * SyncAdapter implementation.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

	/** Debug tag for use logging debug output to LogCat. */
	private static final String TAG = "SyncAdapter";

	private SyncComponent syncComponent;

	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
	}

	@Override
	public void onPerformSync(final Account account, final Bundle extras, final String authority,
			final ContentProviderClient provider, final SyncResult syncResult) {
		final boolean manualSync = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);

		Log.d(TAG, "onPerformSync for account " + account.name + ", manualSync=" + manualSync);

		// Perform a sync using SyncHelper
		if (syncComponent == null) {
			syncComponent = AppComponentService.obtain(getContext()).syncComponent();
		}

		syncComponent.syncHelper().performSync(account, provider, syncResult);
	}
}
