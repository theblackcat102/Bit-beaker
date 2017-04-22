package fi.iki.kuitsi.bitbeaker.preferences;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

import java.util.Locale;

public class LocalePreference extends ListPreference {

	/**
	 *  Alphabetically sorted list of available translations in res/values-{xx}/strings.xml or
	 *  res/values-{xx}-r{YY}/strings.xml.
	 *
	 * TODO: check if all of these are found in {@link java.util.Locale#getAvailableLocales()}.
	 */
	private static final Locale[] TRANSLATIONS = new Locale[] {
		new Locale("cs"),
		Locale.GERMAN,
		Locale.ENGLISH,
		Locale.UK, // dd/mm/yyyy date format with 24h clock
		new Locale("es"),
		new Locale("fi"),
		Locale.FRENCH,
		new Locale("hi"),
		new Locale("hu"),
		new Locale("in"),
		Locale.ITALIAN,
		Locale.JAPANESE,
		Locale.KOREAN,
		new Locale("nl"),
		new Locale("pl"),
		new Locale("pt", "BR"),
		new Locale("ru"),
		new Locale("uk"),
		Locale.SIMPLIFIED_CHINESE
	};

	public LocalePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setEntries(createEntries());
		setEntryValues(createEntryValues());
	}

	private CharSequence[] createEntries() {
		CharSequence[] entries = new CharSequence[TRANSLATIONS.length + 1];
		Locale system = Resources.getSystem().getConfiguration().locale;
		entries[0] = system.getDisplayName(system);
		int i = 1;
		for (Locale lang : TRANSLATIONS) {
			entries[i] = lang.toString() + ": " + lang.getDisplayName(lang);
			i++;
		}
		return entries;
	}

	private CharSequence[] createEntryValues() {
		CharSequence[] entryValues = new CharSequence[TRANSLATIONS.length + 1];
		entryValues[0] = "";
		int i = 1;
		for (Locale lang : TRANSLATIONS) {
			entryValues[i] = lang.toString();
			i++;
		}
		return entryValues;
	}
}
