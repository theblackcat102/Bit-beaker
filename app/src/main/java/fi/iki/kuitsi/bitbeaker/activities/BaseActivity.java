package fi.iki.kuitsi.bitbeaker.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import fi.iki.kuitsi.bitbeaker.Bitbeaker;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.fragments.AboutDialog;

/**
 * Abstract activity that utilizes {@link Toolbar} and sets it to act as the ActionBar of
 * the Activity window. It contains some helper methods and practices:
 * setting content view, injecting views, up navigation, add and replace fragments, show toasts.
 * It can be a successor of MyActivity.
 */
public abstract class BaseActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener {

	private static final String ABOUT_DIALOG_TAG = "dialog_about";
	private static final String CROWDIN_URI_STRING = "https://crowdin.com/project/bitbeaker";
	private static final String BETA_SIGNUP_URI_STRING = "https://play.google.com/apps/testing/fi.iki.kuitsi.bitbeaker";

	@Nullable @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
	@Nullable @BindView(R.id.navigation_view) NavigationView navigationView;
	@Nullable @BindView(R.id.toolbar) Toolbar toolbar;
	@Nullable @BindView(R.id.toolbar_progressbar) ProgressBar toolbarProgressBar;

	private ActionBarDrawerToggle drawerToggle;
	@LayoutRes private final int layoutRes;

	public BaseActivity(@LayoutRes int layoutRes) {
		this.layoutRes = layoutRes;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Bitbeaker.get(this).updateLanguage();
		super.onCreate(savedInstanceState);
		setContentView(layoutRes);

		ButterKnife.bind(this);

		if (toolbar != null) {
			setSupportActionBar(toolbar);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		if (drawerLayout != null) {
			// Set a custom shadow that overlays the main content when the drawer opens
			drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
			// ActionBarDrawerToggle ties together the the proper interactions
			// between the sliding drawer and the action bar app icon
			drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
					R.string.navigationdrawer_open,
					R.string.navigationdrawer_close);
			drawerLayout.addDrawerListener(drawerToggle);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (navigationView != null) {
			navigationView.setNavigationItemSelectedListener(this);
		}
		if (drawerToggle != null) {
			// Sync the toggle state after onRestoreInstanceState has occurred.
			drawerToggle.syncState();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (drawerToggle != null) {
			// Pass any configuration change to the drawer toggles
			drawerToggle.syncState();
		}
	}

	@Override
	public void onDestroy() {
		Crouton.clearCroutonsForActivity(this);
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (drawerToggle != null && drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onSupportNavigateUp() {
		if (!super.onSupportNavigateUp()) {
			finish();
		}
		return true;
	}

	@Nullable
	protected Toolbar getToolbar() {
		return toolbar;
	}

	@SuppressWarnings("unchecked")
	protected <T extends Fragment> T findFragmentById(@IdRes int id) {
		return (T) getSupportFragmentManager().findFragmentById(id);
	}

	protected boolean hasFragment(@IdRes int id) {
		return getSupportFragmentManager().findFragmentById(id) != null;
	}

	protected void setInitialFragment(@IdRes int containerViewId, Fragment fragment,
			@Nullable String tag) {
		if (!hasFragment(containerViewId)) {
			addFragment(containerViewId, fragment, tag);
		}
	}

	protected void addFragment(@IdRes int containerViewId, Fragment fragment,
			@Nullable String tag) {
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.add(containerViewId, fragment, tag);
		fragmentTransaction.commit();
	}

	protected void replaceFragment(@IdRes int containerViewId, Fragment fragment,
			@Nullable String tag) {
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(containerViewId, fragment, tag);
		fragmentTransaction.commit();
	}

	protected final void setToolbarSubtitle(@StringRes int resId) {
		if (toolbar != null) {
			toolbar.setSubtitle(resId);
		}
	}

	protected final void setToolbarSubtitle(CharSequence subtitle) {
		if (toolbar != null) {
			toolbar.setSubtitle(subtitle);
		}
	}

	public void showProgressBar(boolean show) {
		if (toolbarProgressBar != null) {
			if (show) {
				toolbarProgressBar.setVisibility(View.VISIBLE);
			} else {
				toolbarProgressBar.setVisibility(View.GONE);
			}
		}
	}

	protected void showToast(@StringRes int textId) {
		showToast(textId, Style.INFO);
	}

	protected void showToast(@StringRes int textId, Style style) {
		Crouton.showText(this, textId, style);
	}

	protected void showToast(CharSequence text) {
		showToast(text, Style.INFO);
	}

	protected void showToast(CharSequence text, Style style) {
		Crouton.showText(this, text, style);
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem menuItem) {
		if (drawerLayout != null) {
			drawerLayout.closeDrawer(GravityCompat.START);
		}
		switch (menuItem.getItemId()) {
			case R.id.nav_newsfeed:
				startActivityFromDrawer(NewsfeedActivity.class);
				return false;
			case R.id.nav_repositories:
				startActivityFromDrawer(RepositoriesActivity.class);
				return true;
			case R.id.nav_search:
				startActivityFromDrawer(SearchableActivity.class);
				return false;
			case R.id.nav_settings:
				startActivityFromDrawer(SettingsActivity.class);
				return true;
			case R.id.nav_about:
				showAbout();
				return true;
			case R.id.nav_contribute:
				showContributeOptions();
				return true;
			default:
				return false;
		}
	}

	private void startActivityFromDrawer(Class<? extends Activity> activityClass) {
		ActivityCompat.startActivity(this, new Intent(this, activityClass), null);
	}

	private void showAbout() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment prev = fm.findFragmentByTag(ABOUT_DIALOG_TAG);
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		new AboutDialog().show(ft, ABOUT_DIALOG_TAG);
	}

	private void showContributeOptions() {
		final int CONTRIBUTE_TRANSLATIONS = 1;
		final int CONTRIBUTE_ISSUE_TRACKER = 2;
		final int CONTRIBUTE_BETA_RELEASES = 3;
		final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(this);
		adapter.add(new MaterialSimpleListItem.Builder(this)
				.id(CONTRIBUTE_ISSUE_TRACKER)
				.content(R.string.navigationdrawer_issue_tracker)
				.icon(R.drawable.icon_bug)
				.backgroundColor(Color.WHITE)
				.build());
		adapter.add(new MaterialSimpleListItem.Builder(this)
				.id(CONTRIBUTE_TRANSLATIONS)
				.content(R.string.prefs_about_translations_title)
				.icon(R.drawable.ab_icon_translations)
				.backgroundColor(Color.GRAY)
				.build());
		adapter.add(new MaterialSimpleListItem.Builder(this)
				.id(CONTRIBUTE_BETA_RELEASES)
				.content(R.string.contribute_beta_releases)
				.icon(R.drawable.icon_enhancement)//TODO
				.backgroundColor(Color.WHITE)
				.build());

		new MaterialDialog.Builder(this)
			.title(R.string.contribute_dialog_title)
			.adapter(adapter, new MaterialDialog.ListCallback() {
				@Override
				public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
					MaterialSimpleListItem item = adapter.getItem(which);
					switch ((int) item.getId()) {
						case CONTRIBUTE_TRANSLATIONS:
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(CROWDIN_URI_STRING)));
							return;
						case CONTRIBUTE_ISSUE_TRACKER:
							startActivity(IssuesActivity.createIntent(BaseActivity.this,
									Bitbeaker.REPO_OWNER, Bitbeaker.REPO_SLUG));
							return;
						case CONTRIBUTE_BETA_RELEASES:
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BETA_SIGNUP_URI_STRING)));
							return;
						default:
							return;
					}
				}
			})
			.show();
	}
}
