package fi.iki.kuitsi.bitbeaker.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import de.keyboardsurfer.android.widget.crouton.Style;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.fragments.SearchResultsFragment;

public class SearchableActivity extends BaseActivity
	implements SearchView.OnQueryTextListener {

	private static final int MIN_QUERY_LENGTH = 3;

	private final Handler handler = new Handler();
	private SearchView searchView;

	public SearchableActivity() {
		super(R.layout.activity_singlepane_toolbar);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		onNewIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			search(query);
		}
	}

	private void search(String query) {
		if (query.length() >= MIN_QUERY_LENGTH) {
			setTitle(String.format(getString(R.string.search_results_title), query));
			replaceFragment(R.id.fragment_container,
					SearchResultsFragment.newInstance(query),
					SearchResultsFragment.class.getCanonicalName());
		} else {
			showToast(R.string.please_enter_more_text, Style.ALERT);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search, menu);
		final MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
		MenuItemCompat.setShowAsAction(searchMenuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
		searchView.setQueryHint(getString(R.string.search_repo));
		searchView.setOnQueryTextListener(this);
		searchView.setIconifiedByDefault(true);
		handler.post(mShowImeRunnable);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_search:
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onQueryTextSubmit(String s) {
		search(s);
		return true;
	}

	@Override
	public boolean onQueryTextChange(String s) {
		return false;
	}

	private final Runnable mShowImeRunnable = new Runnable() {
		public void run() {
			searchView.setIconified(false);
			searchView.requestFocus();
		}
	};
}
