package fi.iki.kuitsi.bitbeaker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Abstract activity that stores repository related data and helps creating {@link Intent} and
 * {@link Bundle}.
 */
abstract class BaseRepositoryActivity extends BaseActivity {

	private static final String TAG = BaseRepositoryActivity.class.getSimpleName();

	private String owner;
	private String slug;

	public BaseRepositoryActivity(@LayoutRes int layoutRes) {
		super(layoutRes);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getExtras();
	}

	@Nullable
	@Override
	public Intent getSupportParentActivityIntent() {
		Intent intent = super.getSupportParentActivityIntent();
		if (intent == null) {
			Log.e(TAG, "Failed to get parent activity fo " + this);
			return null;
		}
		return addExtendedDataToIntent(intent);
	}

	protected static Intent addExtendedDataToIntent(Intent intent, String owner, String slug) {
		Bundle bundle = createBundle(owner, slug);
		intent.putExtras(bundle);
		return intent;
	}

	protected static Bundle createBundle(String owner, String slug) {
		Bundle bundle = new Bundle();
		bundle.putString("owner", owner);
		bundle.putString("slug", slug);
		return bundle;
	}

	private Intent addExtendedDataToIntent(Intent intent) {
		return addExtendedDataToIntent(intent, owner, slug);
	}

	protected Bundle createBundle() {
		return createBundle(owner, slug);
	}

	protected void getExtras() {
		owner = getIntent().getStringExtra("owner");
		slug = getIntent().getStringExtra("slug");
	}

	protected String getOwner() {
		return owner;
	}

	protected String getSlug() {
		return slug;
	}

}
