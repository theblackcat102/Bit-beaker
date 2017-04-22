package fi.iki.kuitsi.bitbeaker.activities;

import android.os.Bundle;

import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.fragments.SettingsFragment;

public class SettingsActivity extends BaseActivity {

	public SettingsActivity() {
		super(R.layout.activity_singlepane_toolbar);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.settings);
		if (!hasFragment(R.id.fragment_container)) {
			addFragment(R.id.fragment_container, new SettingsFragment(), SettingsFragment.class.getCanonicalName());
		}
	}
}
