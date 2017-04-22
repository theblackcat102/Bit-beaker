package fi.iki.kuitsi.bitbeaker.preferences;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.PeriodicSync;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.TwoStatePreference;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import fi.iki.kuitsi.bitbeaker.AppComponentService;
import fi.iki.kuitsi.bitbeaker.account.AuthenticatedUserManager;
import fi.iki.kuitsi.bitbeaker.provider.BitbeakerContract;

/**
 * Manage sync related preferences.
 */
public class SyncPreferencesManager {

	private static final String TAG = "SyncPreferencesManager";

	private static final String PREF_SYNC_ENABLED = "sync_enabled";
	private static final String PREF_SYNC_INTERVAL = "sync_interval";

	private static final long PREF_DEFAULT_SYNC_INTERVAL = 12L * 60L * 60L;

	private Context applicationContext;
	private TwoStatePreference syncEnable;
	private ListPreferenceWithSummary syncInterval;

	private final Preference.OnPreferenceClickListener syncEnabledClick =
			new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					TwoStatePreference twoStatePreference = (TwoStatePreference) preference;
					setSyncable(twoStatePreference.isChecked());
					syncInterval.setEnabled(twoStatePreference.isChecked());
					return true;
				}
			};

	private final Preference.OnPreferenceChangeListener syncIntervalChange =
			new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					setPeriodicSync(Long.parseLong((String) newValue));
					return true;
				}
			};

	public SyncPreferencesManager() {
	}

	public void attach(PreferenceFragmentCompat fragment) {
		applicationContext = fragment.getContext().getApplicationContext();
		syncEnable = (TwoStatePreference) fragment.findPreference(PREF_SYNC_ENABLED);
		syncInterval = (ListPreferenceWithSummary) fragment.findPreference(PREF_SYNC_INTERVAL);
		init();
	}

	public void detach() {
		syncEnable.setOnPreferenceClickListener(null);
		syncEnable = null;
		syncInterval.setOnPreferenceClickListener(null);
		syncInterval = null;
	}

	private void init() {
		Account account = getAccount();
		if (account != null) {
			boolean isSync = isSyncable();

			syncEnable.setChecked(isSync);
			syncEnable.setOnPreferenceClickListener(syncEnabledClick);

			syncInterval.setEnabled(isSync);
			syncInterval.setOnPreferenceChangeListener(syncIntervalChange);
			String currentSyncInterval = Long.toString(getSyncInterval());
			List<CharSequence> syncIntervalEntries = Arrays.asList(syncInterval.getEntryValues());
			if (syncIntervalEntries.contains(currentSyncInterval)) {
				syncInterval.setValue(currentSyncInterval);
			} else {
				setPeriodicSync(PREF_DEFAULT_SYNC_INTERVAL);
			}
		} else {
			syncEnable.setEnabled(false);
			syncInterval.setEnabled(false);
		}
	}

	@Nullable
	private Account getAccount() {
		if (applicationContext != null) {
			AuthenticatedUserManager userManager = AppComponentService.obtain(applicationContext).userManager();
			return userManager.getAccount();
		}
		return null;
	}

	private boolean isSyncable() {
		Account account = getAccount();
		return account != null && ContentResolver.getSyncAutomatically(account,
				BitbeakerContract.CONTENT_AUTHORITY);
	}

	private void setSyncable(boolean syncable) {
		Account account = getAccount();
		if (account != null) {
			ContentResolver.setIsSyncable(account, BitbeakerContract.CONTENT_AUTHORITY,
					syncable ? 1 : 0);
			ContentResolver.setSyncAutomatically(account, BitbeakerContract.CONTENT_AUTHORITY,
					syncable);
		}
	}

	/**
	 * Set periodic sync.
	 * @param pollFrequency how frequently the sync should be performed, in seconds.
	 */
	private void setPeriodicSync(long pollFrequency) {
		Account account = getAccount();
		if (account != null) {
			Log.d(TAG, "setPeriodicSync " + pollFrequency + "sec");
			ContentResolver.addPeriodicSync(account, BitbeakerContract.CONTENT_AUTHORITY,
					new Bundle(), pollFrequency);
		}
	}

	private long getSyncInterval() {
		Account account = getAccount();
		if (account != null) {
			final List<PeriodicSync> periodicSyncs = ContentResolver.getPeriodicSyncs(account,
					BitbeakerContract.CONTENT_AUTHORITY);
			if (periodicSyncs.size() == 1) {
				return periodicSyncs.get(0).period;
			}
		}
		return 0L;
	}
}
