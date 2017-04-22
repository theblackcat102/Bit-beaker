package fi.iki.kuitsi.bitbeaker.activities;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.request.listener.SpiceServiceListener;

import fi.iki.kuitsi.bitbeaker.Bitbeaker;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.event.RefreshEvent;
import fi.iki.kuitsi.bitbeaker.fragments.RepositoriesFragment;
import fi.iki.kuitsi.bitbeaker.network.RestService;
import fi.iki.kuitsi.bitbeaker.network.SpiceServiceListenerAdapter;

public class RepositoriesActivity extends BaseActivity {

	private MenuItem refreshItem;
	private final SpiceManager spiceManager = new SpiceManager(RestService.class);
	private final SpiceServiceListener spiceServiceListener =
			new SpiceServiceListenerAdapter() {
				@Override
				protected void onIdle() {
					if (refreshItem != null) {
						MenuItemCompat.setActionView(refreshItem, null);
					}
				}

				@Override
				protected void onActive() {
					if (refreshItem != null) {
						MenuItemCompat.setActionView(refreshItem, R.layout.actionview_loading);
					}
				}
			};

	public RepositoriesActivity() {
		super(R.layout.activity_singlepane_drawer_toolbar);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(this.getString(R.string.your_repositories));
		setInitialFragment(R.id.fragment_container, RepositoriesFragment.newInstance(),
				RepositoriesFragment.class.getCanonicalName());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.repositories, menu);
		refreshItem = menu.findItem(R.id.menu_refresh);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_refresh) {
			Bitbeaker.getEventBus().post(new RefreshEvent());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
		super.onStart();
		spiceManager.start(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		spiceManager.addSpiceServiceListener(spiceServiceListener);
	}

	@Override
	public void onPause() {
		spiceManager.removeSpiceServiceListener(spiceServiceListener);
		super.onPause();
	}

	@Override
	protected void onStop() {
		spiceManager.shouldStop();
		super.onStop();
	}
}
