package fi.iki.kuitsi.bitbeaker.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import fi.iki.kuitsi.bitbeaker.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

	private final List<Fragment> fragments;
	private final List<String> titles;

	public SimpleFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		this.fragments = new ArrayList<Fragment>();
		this.titles = new ArrayList<String>();
	}

	/**
	 * Add fragment to the pager adapter.
	 *
	 * @param fragment Fragment to add.
	 * @param title Title of fragment.
	 */
	public void addPage(Fragment fragment, String title) {
		fragments.add(fragment);
		Locale l = Helper.getCurrentLocale();
		titles.add(title.toUpperCase(l));
	}

	/**
	 * Return the Fragment associated with a specified position.
	 */
	@Override
	public Fragment getItem(int position) {
		return fragments.get(position);
	}

	/**
	 * Return the number of fragments available.
	 */
	@Override
	public int getCount() {
		return fragments.size();
	}

	/**
	 * Return the title string to describe the specified fragment.
	 *
	 * @param position The position of the title requested.
	 */
	@Override
	public CharSequence getPageTitle(int position) {
		return titles.get(position);
	}

	/**
	 * Update page title.
	 *
	 * @param position The position (in backing array) of the title to be changed.
	 * @param newTitle New title.
	 */
	public void setPageTitle(int position, String newTitle) {
		if (newTitle != null) {
			Locale l = Helper.getCurrentLocale();
			newTitle = newTitle.toUpperCase(l);
		}
		titles.set(position, newTitle);
		notifyDataSetChanged();
	}
}

