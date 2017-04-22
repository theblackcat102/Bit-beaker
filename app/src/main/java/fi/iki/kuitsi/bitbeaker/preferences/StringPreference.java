package fi.iki.kuitsi.bitbeaker.preferences;

import android.content.SharedPreferences;

/**
 * Wrapper class for a string preference.
 * @see AbstractPreference
 */
public class StringPreference extends AbstractPreference<String> {

	public StringPreference(SharedPreferences preferences, String key) {
		super(preferences, key, "");
	}

	public StringPreference(SharedPreferences preferences, String key, String defaultValue) {
		super(preferences, key, defaultValue);
	}

	@Override
	public String get() {
		return getPreferences().getString(getKey(), getDefaultValue());
	}

	@Override
	public void set(String value) {
		getPreferences().edit().putString(getKey(), value).apply();
	}
}
