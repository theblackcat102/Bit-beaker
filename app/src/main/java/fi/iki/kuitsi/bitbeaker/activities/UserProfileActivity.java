package fi.iki.kuitsi.bitbeaker.activities;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import butterknife.BindView;
import fi.iki.kuitsi.bitbeaker.AppComponentService;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.adapters.RepositoriesAdapter;
import fi.iki.kuitsi.bitbeaker.data.ImageLoader;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;
import fi.iki.kuitsi.bitbeaker.domainobjects.User;
import fi.iki.kuitsi.bitbeaker.network.RestService;
import fi.iki.kuitsi.bitbeaker.network.request.users.AccountProfileRequest;
import fi.iki.kuitsi.bitbeaker.network.response.users.AccountProfile;

/**
 * Display user profile: avatar, user name and list of repositories.
 */
public final class UserProfileActivity extends BaseActivity {

	private static final int ID_GROUP_SORT = 1;

	@BindView(R.id.real_name) TextView realName;
	@BindView(R.id.avatar) ImageView avatar;
	@BindView(R.id.repositories_header) TextView listHeader;
	@BindView(R.id.repositories) ListView listView;

	private final SpiceManager spiceManager = new SpiceManager(RestService.class);
	private RepositoriesAdapter adapter;
	private String user;
	private ImageLoader imageLoader;

	public UserProfileActivity() {
		super(R.layout.activity_userprofile);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		user = getIntent().getStringExtra("user");
		setTitle(user);

		adapter = new RepositoriesAdapter(this);
		listView.setAdapter(adapter);

		imageLoader = AppComponentService.obtain(getApplicationContext()).imageLoader();
	}

	@Override
	protected void onStart() {
		super.onStart();
		spiceManager.start(this);
		performRequest();
	}

	@Override
	protected void onStop() {
		spiceManager.shouldStop();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu sub = menu.addSubMenu(R.string.repositories_sorting_order);
		sub.setIcon(R.drawable.ab_icon_sort);
		MenuItemCompat.setShowAsAction(sub.getItem(), MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		sub.add(ID_GROUP_SORT, RepositoriesAdapter.Sort.REPO_NAME_ASC.ordinal(), Menu.NONE,
				RepositoriesAdapter.Sort.REPO_NAME_ASC.toString(getResources()));
		sub.add(ID_GROUP_SORT, RepositoriesAdapter.Sort.UPDATED_DESC.ordinal(), Menu.NONE,
				RepositoriesAdapter.Sort.UPDATED_DESC.toString(getResources()));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getGroupId() == ID_GROUP_SORT) {
			// Don't save this selection to SharedPreferences like in RepositoriesActivity (or at
			// least not in same preference) as we are not using all available sort options here.
			adapter.sort(RepositoriesAdapter.Sort.values()[item.getItemId()]);
			return true;
		}
		return super.onOptionsItemSelected(item);

	}

	private void performRequest() {
		showProgressBar(true);
		AccountProfileRequest request = new AccountProfileRequest(user);
		spiceManager.execute(request, request.getCacheKey(), request.getCacheExpireDuration(),
				new AccountProfileRequestListener());
	}

	private void displayAccountProfile(User user, Repository.List repositories) {
		realName.setText(user.getDisplayName());
		imageLoader.loadAvatar(this, user.getAvatarUrl(), avatar);
		adapter.clear();
		if (repositories.isEmpty()) {
			listHeader.setVisibility(View.INVISIBLE);
		} else {
			listHeader.setVisibility(View.VISIBLE);
			adapter.addAll(repositories);
		}
	}

	final class AccountProfileRequestListener implements RequestListener<AccountProfile> {
		@Override
		public void onRequestFailure(SpiceException e) {
			showProgressBar(false);
		}

		@Override
		public void onRequestSuccess(AccountProfile accountProfile) {
			User user = accountProfile.getUser();
			Repository.List repositories = accountProfile.getRepositories();
			displayAccountProfile(user, repositories);
			showProgressBar(false);
		}
	}
}
