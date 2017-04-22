package fi.iki.kuitsi.bitbeaker.preferences;

import android.content.SharedPreferences;

/**
 * Wrapper class for an integer preference.
 * @see AbstractPreference
 */
public class IntPreference extends AbstractPreference<Integer> {

	public IntPreference(SharedPreferences preferences, String key) {
		super(preferences, key, 0);
	}

	public IntPreference(SharedPreferences preferences, String key, int defaultValue) {
		super(preferences, key, defaultValue);
	}

	@Override
	public Integer get() {
		return getPreferences().getInt(getKey(), getDefaultValue());
	}

	@Override
	public void set(Integer value) {
		getPreferences().edit().putInt(getKey(), value).apply();
	}
}
