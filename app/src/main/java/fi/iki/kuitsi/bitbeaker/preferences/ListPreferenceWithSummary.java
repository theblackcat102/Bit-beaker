package fi.iki.kuitsi.bitbeaker.preferences;

import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

/**
 * A {@link ListPreference}. Its summary is the selected entry.
 */
public class ListPreferenceWithSummary extends ListPreference {

	public ListPreferenceWithSummary(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (enabled) {
			super.setSummary(getEntry());
		} else {
			super.setSummary("");
		}
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
		setSummary(value);
	}

	@Override
	public void setSummary(CharSequence summary) {
		super.setSummary(getEntry());
	}
}
