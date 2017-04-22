package fi.iki.kuitsi.bitbeaker.preferences;

/**
 * Internal interface that allows a SettingsFragment to communicate up to its PreferencesActivity.
 */
public interface LoginStateChangedListener {
	void onLoginStateChanged();
}
