package fi.iki.kuitsi.bitbeaker.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import fi.iki.kuitsi.bitbeaker.ActivityComponent;
import fi.iki.kuitsi.bitbeaker.ActivityComponentFactory;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.domainobjects.Changeset;
import fi.iki.kuitsi.bitbeaker.network.request.BitbucketRequest;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.BranchListRequest;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.ChangesetRequest;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.BranchNames;

import java.util.List;

public class LauncherActivity extends Activity {

	private ActivityComponent activityComponent;
	private SpiceManager spiceManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		activityComponent = ActivityComponentFactory.create(this);
		spiceManager = activityComponent.spiceManager();

		Uri data = getIntent().getData();
		if (data != null) {
			if (handleUriData(data)) {
				setContentView(R.layout.login);
				View buttonBar = findViewById(R.id.button_bar);
				if (buttonBar != null) {
					buttonBar.setVisibility(View.GONE);
				}
				return;
			}
		}
		this.finish();
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
	protected void onDestroy() {
		activityComponent = null;
		super.onDestroy();
	}

	private boolean handleUriData(Uri data) {
		Bundle b = new Bundle();
		String user = "";
		String slug = "";
		List<String> segments = data.getPathSegments();
		if (segments.size() >= 2) {
			user = segments.get(0);
			if (user.equals("dashboard") || user.equals("account") || user.equals("repo")) {
				makeToast(R.string.link_not_supported);
				return false;
			}
			slug = segments.get(1);
			b.putString("owner", user);
			b.putString("slug", slug);
		}

		// changesets
		if (segments.size() > 3 && (segments.get(2).equalsIgnoreCase("commits")
				|| segments.get(2).equalsIgnoreCase("changeset"))) {
			// get node data from API
			ChangesetRequest request = new ChangesetRequest(user, slug, segments.get(3));
			performRequest(request, new ChangesetRequestListener(user, slug));
			return true;
		}

		// source code browser
		else if (segments.size() == 3 && segments.get(2).equalsIgnoreCase("src")) {
			// get branches for SourceBrowserActivity
			BranchListRequest request = new BranchListRequest(user, slug);
			performRequest(request, new BranchListRequestListener(user, slug));
			return true;
		}

		// probably repository's main page
		else if (segments.size() == 2 ||
			(segments.size() == 3 && segments.get(2).equalsIgnoreCase("overview")) ||
			(segments.size() == 3 && segments.get(2).equalsIgnoreCase("commits"))) {
			Intent intent = new Intent(this, RepositoryActivity.class);
			intent.putExtras(b);
			this.startActivity(intent);
		}

		// user profile
		else if (segments.size() == 1) {
			b.putString("user", segments.get(0));
			Intent intent = new Intent(this, UserProfileActivity.class);
			intent.putExtras(b);
			this.startActivity(intent);
		}

		// wiki
		else if (segments.size() >= 3 && segments.get(2).equalsIgnoreCase("wiki")) {
			String file = null;
			if (segments.size() > 3) {
				// Segments: 0=user, 1=slug and 2=wiki. The rest are file path inside wiki
				StringBuilder path = new StringBuilder();
				for (int i = 3; i < segments.size(); i++) {
					path.append(segments.get(i));
					path.append("/");
				}
				path.deleteCharAt(path.length()-1);// remove last "/"
				file = path.toString();
			}
			startActivity(WikiActivity.createIntent(this, user, slug, file));
		}

		// pull requests
		else if (segments.size() == 3 && segments.get(2).equalsIgnoreCase("pull-requests")) {
			Intent intent = new Intent(this, PullRequestActivity.class);
			intent.putExtras(b);
			this.startActivity(intent);
		}

		// pull request comment
		else if (segments.size() >= 4 && segments.get(2).equalsIgnoreCase("pull-request")) {
			b.putInt("pullRequestId", Integer.parseInt(segments.get(3)));
			Intent intent = new Intent(this, PullRequestCommentActivity.class);
			intent.putExtras(b);
			this.startActivity(intent);
		}

		else {
			makeToast(R.string.link_not_supported);
		}
		return false;
	}

	protected void makeToast(@StringRes int textId) {
		Toast.makeText(this, getString(textId), Toast.LENGTH_LONG).show();
	}

	protected <T> void performRequest(BitbucketRequest<T> request,
			RequestListener<T> requestListener) {
		spiceManager.execute(request, request.getCacheKey(), request.getCacheExpireDuration(),
				requestListener);
	}

	private class BranchListRequestListener implements RequestListener<BranchNames> {

		private final String user;
		private final String slug;

		public BranchListRequestListener(String user, String slug) {
			this.user = user;
			this.slug = slug;
		}

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			makeToast(R.string.no_branches);
			finish();
		}

		@Override
		public void onRequestSuccess(BranchNames branchNames) {
			final String[] branches = branchNames.toArray();
			final Intent intent = SourceBrowserActivity.createIntent(LauncherActivity.this, user,
					slug, branches, branches[0], "/");
			startActivity(intent);
			finish();
		}
	}

	private class ChangesetRequestListener implements  RequestListener<Changeset> {

		private final String user;
		private final String slug;

		private ChangesetRequestListener(String user, String slug) {
			this.user = user;
			this.slug = slug;
		}

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			makeToast(R.string.AsyncErrorMSG);
			finish();
		}

		@Override
		public void onRequestSuccess(Changeset changeset) {
			final Intent intent = ChangesetActivity.createIntent(LauncherActivity.this, user, slug,
					changeset, null);
			startActivity(intent);
			finish();
		}
	}

}
