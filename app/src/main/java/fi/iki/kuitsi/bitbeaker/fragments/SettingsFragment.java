package fi.iki.kuitsi.bitbeaker.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.octo.android.robospice.SpiceManager;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.network.RestService;
import fi.iki.kuitsi.bitbeaker.preferences.DialogPreference;
import fi.iki.kuitsi.bitbeaker.preferences.FavoritesManager;
import fi.iki.kuitsi.bitbeaker.preferences.SyncPreferencesManager;

public class SettingsFragment extends PreferenceFragmentCompat {

	private static final String TAG = SettingsFragment.class.getSimpleName();

	private final SpiceManager spiceManager = new SpiceManager(RestService.class);
	private final SyncPreferencesManager syncPreferencesManager = new SyncPreferencesManager();
	private final FavoritesManager favoritesManager = new FavoritesManager();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		addPreferencesFromResource(R.xml.preferences_ui);
		addPreferencesFromResource(R.xml.preferences_favorites);
		addPreferencesFromResource(R.xml.preferences_sync);
	}

	@Override
	public void onStart() {
		super.onStart();
		spiceManager.start(getContext());
		syncPreferencesManager.attach(this);
		favoritesManager.attach(this);
	}

	@Override
	public void onStop() {
		spiceManager.shouldStop();
		syncPreferencesManager.detach();
		favoritesManager.detach();
		super.onStop();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.settings, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (R.id.menu_clear_cache == item.getItemId()) {
			new ClearCacheTask().execute();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		favoritesManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	public void onDisplayPreferenceDialog(Preference preference) {
		if (preference instanceof DialogPreference) {
			favoritesManager.onDisplayPreferenceDialog((DialogPreference) preference);
		} else {
			super.onDisplayPreferenceDialog(preference);
		}
	}

	private class ClearCacheTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			Future<?> command = spiceManager.removeAllDataFromCache();
			if (command != null) {
				try {
					command.get();
					return true;
				} catch (InterruptedException | ExecutionException e) {
					Log.e(TAG, "Failed to execute clear cache command", e);
					return false;
				}
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// NOTE Toast can be displayed here
		}
	}
}
