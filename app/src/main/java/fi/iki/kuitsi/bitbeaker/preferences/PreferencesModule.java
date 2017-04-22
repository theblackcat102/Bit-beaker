package fi.iki.kuitsi.bitbeaker.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import fi.iki.kuitsi.bitbeaker.adapters.RepositoriesAdapter;

public class PreferencesModule {

	private PreferencesModule() { }

	public static final String LOCALE_OVERRIDE = "locale_override";
	public static final String NEWSFEED_TOKEN = "newsfeedToken";
	public static final String REPOSITORY_SORT_ORDER = "repositories_sort_order";

	private static SharedPreferences provideSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static StringPreference provideLocalOverridePreference(Context context) {
		return new StringPreference(provideSharedPreferences(context), LOCALE_OVERRIDE);
	}

	public static StringPreference provideNewsfeedToken(Context context) {
		return new StringPreference(provideSharedPreferences(context), NEWSFEED_TOKEN);
	}

	public static IntPreference provideRepositorySortOrder(Context context) {
		return new IntPreference(provideSharedPreferences(context), REPOSITORY_SORT_ORDER,
				RepositoriesAdapter.Sort.UPDATED_DESC.ordinal());
	}
}
