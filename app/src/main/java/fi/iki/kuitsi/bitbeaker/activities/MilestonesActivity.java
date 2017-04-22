package fi.iki.kuitsi.bitbeaker.activities;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.octo.android.robospice.request.listener.SpiceServiceListener;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.adapters.SimpleFragmentPagerAdapter;
import fi.iki.kuitsi.bitbeaker.fragments.issueContainer.ComponentsFragment;
import fi.iki.kuitsi.bitbeaker.fragments.issueContainer.MilestonesFragment;
import fi.iki.kuitsi.bitbeaker.fragments.issueContainer.VersionsFragment;
import fi.iki.kuitsi.bitbeaker.network.SpiceServiceListenerAdapter;

public class MilestonesActivity extends MyActivity {

	private MenuItem refreshItem;

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

	protected String slug;
	protected String owner;

	private SimpleFragmentPagerAdapter fragmentPagerAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle b = getIntent().getExtras();
		slug = b.getString("slug");
		owner = b.getString("owner");

		setContentView(R.layout.view_pager);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(slug);
		actionBar.setSubtitle(owner);

		// Create the adapter that will return a fragment for each sections.
		fragmentPagerAdapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager());
		fragmentPagerAdapter.addPage(
				MilestonesFragment.newInstance(owner, slug),
				getString(R.string.milestones));
		fragmentPagerAdapter.addPage(
				VersionsFragment.newInstance(owner, slug),
				getString(R.string.versions));
		fragmentPagerAdapter.addPage(
				ComponentsFragment.newInstance(owner, slug),
				getString(R.string.components));

		ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(fragmentPagerAdapter);

		// Set up the View Pager title strip.
		PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
		pagerTabStrip.setDrawFullUnderline(true);
		pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.bitbeaker_control));
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

}
