package fi.iki.kuitsi.bitbeaker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.OnClick;
import fi.iki.kuitsi.bitbeaker.AppComponentService;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;
import fi.iki.kuitsi.bitbeaker.domainobjects.User;
import retrofit2.Call;
import retrofit2.Callback;

import java.net.HttpURLConnection;

public final class LoginActivity extends BaseActivity {

	private static final String TAG = LoginActivity.class.getSimpleName();
	private static final int GRANT_ACCESS_REQUEST = 1;

	@BindView(R.id.login_progressBar) ProgressBar progressBar;
	@BindView(R.id.button_bar) View buttonBar;
	private boolean retryLogin = false;
	private BitbucketService bitbucketService;

	public LoginActivity() {
		super(R.layout.login);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bitbucketService = AppComponentService.obtain(getApplicationContext()).bitbucketService();
	}

	@Override
	public void onStart() {
		super.onStart();
		executeLogin();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mainmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_search:
				ActivityCompat.startActivity(this, new Intent(this, SearchableActivity.class), null);
				return true;
			case R.id.menu_settings:
				ActivityCompat.startActivity(this, new Intent(this, SettingsActivity.class), null);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GRANT_ACCESS_REQUEST && resultCode == RESULT_OK) {
			logInOK();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@OnClick(R.id.login_button)
	public void onClickLogin() {
		if (retryLogin) {
			executeLogin();
		} else {
			Intent intent = new Intent(LoginActivity.this, GrantAccessActivity.class);
			startActivityForResult(intent, GRANT_ACCESS_REQUEST);
		}
	}

	protected void executeLogin() {
		buttonBar.setVisibility(View.INVISIBLE);
		progressBar.setVisibility(View.VISIBLE);
		bitbucketService.user().enqueue(new Callback<User>() {
			@Override
			public void onResponse(Call<User> call, retrofit2.Response<User> response) {
				progressBar.setVisibility(View.INVISIBLE);
				if (response.isSuccessful()) {
					logInOK();
				} else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
					buttonBar.setVisibility(View.VISIBLE);
				} else {
					buttonBar.setVisibility(View.VISIBLE);
					retryLogin = true;
				}
			}

			@Override
			public void onFailure(Call<User> call, Throwable t) {
				Log.e(TAG, "onFailure", t);
				progressBar.setVisibility(View.INVISIBLE);
				buttonBar.setVisibility(View.VISIBLE);
				retryLogin = true;
				showToast(R.string.AsyncErrorMSG);
			}
		});
	}

	void logInOK() {
		startActivity(new Intent(this, RepositoriesActivity.class));
		finish();
	}
}
