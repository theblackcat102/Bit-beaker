package fi.iki.kuitsi.bitbeaker.preferences;

import android.content.SharedPreferences;

/**
 * Represents a key-value item (a.k.a. preference) stored in file pointed by a
 * {@link SharedPreferences} objected.
 * @param <T> Type of preference value.
 */
abstract class AbstractPreference<T> {
	private final SharedPreferences preferences;
	private final String key;
	private final T defaultValue;

	AbstractPreference(SharedPreferences preferences, String key, T defaultValue) {
		this.preferences = preferences;
		this.key = key;
		this.defaultValue = defaultValue;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + '{'
				+ "preferences=" + preferences
				+ ", key='" + key + '\''
				+ ", value='" + get() + '\''
				+ ", defaultValue='" + defaultValue + '\''
				+ '}';
	}

	protected SharedPreferences getPreferences() {
		return preferences;
	}

	public String getKey() {
		return key;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Returns the preference value if it exists, or {@link #defaultValue}. Throws
	 * ClassCastException if there is a preference with this name that is not a {@link T}.
	 * @throws ClassCastException
	 */
	public abstract T get() throws ClassCastException;

	/**
	 * Checks whether the preferences contains this {@link #key}.
	 */
	public boolean isSet() {
		return preferences.contains(key);
	}

	/**
	 * Set preferences value.
	 * @param value The new value for the preference.
	 */
	public abstract void set(T value);

	/**
	 * Remove preference value.
	 */
	public void remove() {
		preferences.edit().remove(key).apply();
	}
}
