package fi.iki.kuitsi.bitbeaker.activities;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.octo.android.robospice.SpiceManager;
import fi.iki.kuitsi.bitbeaker.Bitbeaker;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import fi.iki.kuitsi.bitbeaker.network.RestService;

public abstract class MyActivity extends ActionBarActivity {

	public static final String API_BASE_URL = "https://bitbucket.org/api/1.0";

	protected Bitbeaker bitbeaker;
	protected final SpiceManager spiceManager = new SpiceManager(RestService.class);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Bitbeaker.get(this).updateLanguage();
		super.onCreate(savedInstanceState);
		bitbeaker = (Bitbeaker) getApplication();
		System.setProperty("http.keepAlive", "false");
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		spiceManager.start(this);
	}

	@Override
	protected void onStop() {
		spiceManager.shouldStop();
		super.onStop();
	}

	@Override
	public void onDestroy() {
		Crouton.clearCroutonsForActivity(this);
		super.onDestroy();
	}

	/**
	 * Shows a context sensitive toast ("Crouton") with {@link Style#INFO}.
	 *
	 * @param textId
	 */
	public void makeCrouton(@StringRes int textId) {
		this.makeCrouton(textId, Style.INFO);
	}

	public void makeCrouton(String text) {
		this.makeCrouton(text, Style.INFO);
	}

	public void makeCrouton(@StringRes int textId, Style style) {
		this.makeCrouton(this.getString(textId), style);
	}

	public void makeCrouton(String text, Style style) {
		Crouton.makeText(this, text, style).show();
	}

	/**
	 * Set Activity's title.
	 * <p/>
	 * Support library ActionBar (v7 appcompat) does not show Activity's title in ActionBar
	 *
	 * @param title title
	 * @see <a href="https://code.google.com/p/android/issues/detail?id=58982">Android issue 58982</a>
	 */
	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		getSupportActionBar().setTitle(title);
	}

	/**
	 * Toggle indeterminate ProgressBar visibility.
	 *
	 * @param visible true/false
	 * @deprecated Window provided Progress Bars are deprecated with
	 * {@link android.support.v7.widget.Toolbar}.
	 */
	@Deprecated
	protected void showProgressBar(boolean visible) {

	}

}
