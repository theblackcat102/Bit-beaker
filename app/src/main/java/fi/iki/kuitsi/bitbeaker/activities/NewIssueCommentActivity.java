package fi.iki.kuitsi.bitbeaker.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.network.RestService;
import fi.iki.kuitsi.bitbeaker.network.request.BitbucketRequest;

import butterknife.BindView;

public class NewIssueCommentActivity extends BaseRepositoryActivity {

	@BindView(R.id.new_issue_comment_content) EditText commentEditText;

	private final SpiceManager spiceManager = new SpiceManager(RestService.class);
	private int id;

	public NewIssueCommentActivity() {
		super(R.layout.new_issue_comment);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(R.string.add_comment);
		setToolbarSubtitle(getSlug() + " - #" + id);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_simple_submit, menu);
		return true;
	}

	@Override
	protected void getExtras() {
		super.getExtras();
		id = getIntent().getExtras().getInt("id");
	}

	@Nullable
	@Override
	public Intent getSupportParentActivityIntent() {
		Intent intent = super.getSupportParentActivityIntent();
		if (intent != null) {
			intent.putExtra("id", id);
		}
		return intent;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_submit:
				submit();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void submit() {
		final CharSequence content = commentEditText.getText();
		spiceManager.execute(
				new BitbucketRequest<Void>(Void.class) {
					@Override
					public Void loadDataFromNetwork() throws Exception {
						getService().newIssueComment(getOwner(), getSlug(), id, content).loadDataFromNetwork();
						return null;
					}
				},
				new RequestListener<Void>() {
					@Override
					public void onRequestFailure(SpiceException e) {
						showProgressBar(false);
					}

					@Override
					public void onRequestSuccess(Void aVoid) {
						showProgressBar(false);
						done();
					}
				});
	}

	protected void done() {
		setResult(Activity.RESULT_OK, getIntent());
		finish();
	}

}
