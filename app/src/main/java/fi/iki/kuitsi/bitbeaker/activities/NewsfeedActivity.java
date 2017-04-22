package fi.iki.kuitsi.bitbeaker.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.request.listener.SpiceServiceListener;

import java.util.List;

import butterknife.BindView;
import fi.iki.kuitsi.bitbeaker.Bitbeaker;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.event.RefreshEvent;
import fi.iki.kuitsi.bitbeaker.fragments.NewsfeedFragment;
import fi.iki.kuitsi.bitbeaker.fragments.NewsfeedHelpDialogFragment;
import fi.iki.kuitsi.bitbeaker.network.RssService;
import fi.iki.kuitsi.bitbeaker.network.SpiceServiceListenerAdapter;
import fi.iki.kuitsi.bitbeaker.preferences.PreferencesModule;
import fi.iki.kuitsi.bitbeaker.preferences.StringPreference;
import fi.iki.kuitsi.bitbeaker.util.StringUtils;
import fi.iki.kuitsi.bitbeaker.view.SwipeRefreshLayout;

/**
 * This activity displays the user's main newsfeed using the RSS stream.
 */
public class NewsfeedActivity extends BaseActivity
		implements SwipeRefreshLayout.CanChildScrollUpCallback {

	private static final String DIALOG_TAG = "dialog";
	//private static final String url = "https://bitbucket.org/dashboard/overview";

	@BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;

	private NewsfeedFragment newsfeedFragment;
	private StringPreference newsfeedToken;
	private final SpiceManager spiceManager = new SpiceManager(RssService.class);
	private final SpiceServiceListener spiceServiceListener =
			new SpiceServiceListenerAdapter() {
				@Override
				protected void onIdle() {
					if (swipeRefreshLayout != null) {
						swipeRefreshLayout.setRefreshing(false);
					}
				}
			};

	public NewsfeedActivity() {
		super(R.layout.activity_singlepane_swiperefresh_toolbar);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// check if we can extract new token from Uri
		Uri data = getIntent().getData();
		if (data != null && !getIntent().hasExtra("token")) {
			List<String> segments = data.getPathSegments();
			final String token = data.getQueryParameter("token");
			if (segments.size() == 3 && segments.get(1).equals("rss") && token != null) {
				// we have enough data for reloading the Activity
				Bundle b = new Bundle();
				b.putString("owner", segments.get(0));
				b.putString("token", token);
				Intent intent = new Intent(this, NewsfeedActivity.class);
				intent.putExtras(b);
				finish();
				startActivity(intent);
			}
		}

		newsfeedToken = PreferencesModule.provideNewsfeedToken(this);
		setTitle(R.string.user_newsfeed);
		setInitialFragment(R.id.fragment_container, new NewsfeedFragment(),
				NewsfeedFragment.class.getCanonicalName());
	}

	@Override
	protected void onStart() {
		super.onStart();
		spiceManager.start(this);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		swipeRefreshLayout.setColorSchemeColors(
				getResources().getColor(R.color.bitbeaker_control),
				getResources().getColor(R.color.bitbeaker_accent));
		swipeRefreshLayout.setCanChildScrollUpCallback(this);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				Bitbeaker.getEventBus().post(new RefreshEvent());
			}
		});
		newsfeedFragment = findFragmentById(R.id.fragment_container);
		onNewIntent(getIntent());
	}

	@Override
	public void onResume() {
		super.onResume();
		spiceManager.addSpiceServiceListener(spiceServiceListener);
	}

	@Override
	public void onPause() {
		super.onPause();
		spiceManager.removeSpiceServiceListener(spiceServiceListener);
	}

	@Override
	protected void onStop() {
		spiceManager.shouldStop();
		super.onStop();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Bundle b = intent.getExtras();
		final String owner = getOwner(b);
		final String token = getToken(b);

		if (StringUtils.isNotBlank(token)) {
			newsfeedFragment.reload(owner, token);
		} else {
			FragmentManager fm = getSupportFragmentManager();
			DialogFragment dialog = (DialogFragment) fm.findFragmentByTag(DIALOG_TAG);
			if (dialog == null) {
				dialog = new NewsfeedHelpDialogFragment();
				dialog.show(fm, DIALOG_TAG);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_newsfeed, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				Bitbeaker.getEventBus().post(new RefreshEvent());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Determines the user whose newsfeed is viewed. Primarily the one
	 * set in the intent, but if that's not present, then use the one who
	 * is logged in into the app.
	 *
	 * @param b Intent extras
	 * @return Username of the user whose newsfeed we should load
	 */
	private String getOwner(Bundle b) {
		if (b == null) return Bitbeaker.get(this).getUsername();
		final String ownerParam = b.getString("owner");
		if (StringUtils.isNotBlank(ownerParam)) {
			return ownerParam;
		}
		return Bitbeaker.get(this).getUsername();
	}

	/**
	 * Determines the token that is needed to view the RSS feed.
	 * Primarily take it from the intent that started this activity,
	 * but if it isn't there, load it from the preferences.
	 * Save the token from the intent to the preferences, if possible.
	 *
	 * @param b Intent extras
	 * @return The token, or null if it wasn't found
	 */
	private String getToken(Bundle b) {
		if (b == null) return newsfeedToken.get();
		final String newToken = b.getString("token");
		final String oldToken = newsfeedToken.get();

		if (StringUtils.isNotBlank(newToken)) {
			// Save this token:
			newsfeedToken.set(newToken);
			if (!newToken.equals(oldToken)) {
				SnackbarManager.show(Snackbar.with(getApplicationContext())
								.text(R.string.newsfeed_token_saved), this);
			}
		}

		return newsfeedToken.get();
	}

	@Override
	public boolean canSwipeRefreshChildScrollUp() {
		if (newsfeedFragment != null) {
			return newsfeedFragment.canScrollUp();
		}
		return false;
	}
}
