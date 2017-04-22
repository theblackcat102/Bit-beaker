package fi.iki.kuitsi.bitbeaker.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.adapters.SimpleFragmentPagerAdapter;

/**
 * Fragment that hosts a {@link ViewPager}.
 */
public abstract class BaseTabFragment extends Fragment {

	private static final String SAVE_CURRENT_TAB = "current_tab";

	@BindView(R.id.pager) ViewPager viewPager;
	@BindView(R.id.pager_title_strip) PagerTabStrip pagerTabStrip;
	private Unbinder unbinder;
	private SimpleFragmentPagerAdapter adapter;
	private int currentTab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new SimpleFragmentPagerAdapter(getChildFragmentManager());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.view_pager, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		unbinder = ButterKnife.bind(this, view);

		// Set up the View Pager title strip.
		pagerTabStrip.setDrawFullUnderline(true);
		pagerTabStrip.setTabIndicatorColorResource(R.color.bitbeaker_control);

		// Set up the ViewPager.
		viewPager.setAdapter(adapter);
		if (savedInstanceState != null) {
			currentTab = savedInstanceState.getInt(SAVE_CURRENT_TAB);
			viewPager.setCurrentItem(currentTab);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		currentTab = viewPager.getCurrentItem();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SAVE_CURRENT_TAB, currentTab);
	}

	@Override
	public void onDestroyView() {
		unbinder.unbind();
		super.onDestroyView();
	}

	public void addFragment(Fragment fragment, String title) {
		adapter.addPage(fragment, title);
	}
}
