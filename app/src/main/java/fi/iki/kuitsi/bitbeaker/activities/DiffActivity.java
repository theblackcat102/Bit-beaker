package fi.iki.kuitsi.bitbeaker.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import butterknife.BindView;
import fi.iki.kuitsi.bitbeaker.AppComponentService;
import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.data.api.ApiImageLoader;
import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;
import fi.iki.kuitsi.bitbeaker.data.api.interactor.DiffRequest;
import fi.iki.kuitsi.bitbeaker.data.api.interactor.RawFileStreamRequest;
import fi.iki.kuitsi.bitbeaker.data.remote.BitbucketApiCallCompiler;
import fi.iki.kuitsi.bitbeaker.domainobjects.ChangesetFile.Type;
import fi.iki.kuitsi.bitbeaker.network.RestService;
import fi.iki.kuitsi.bitbeaker.util.StringUtils;

public final class DiffActivity extends BaseActivity {

	@BindView(R.id.diff_webview) WebView webView;
	@BindView(R.id.image_wrapper) LinearLayout diff_image_wrapper;
	@BindView(R.id.diff_image) ImageView diff_image;
	@BindView(R.id.diff_image_old) ImageView diff_image_old;
	private final SpiceManager spiceManager = new SpiceManager(RestService.class);

	public DiffActivity() {
		super(R.layout.diff);
	}

	/**
	 * Create an intent for this activity.
	 */
	public static Intent createIntent(Context context, String owner, String slug, String changesetId,
			String file, Type type, String parentChangesetId) {
		Intent intent = new Intent(context, DiffActivity.class);
		intent.putExtra("owner", owner);
		intent.putExtra("slug", slug);
		intent.putExtra("changeset", changesetId);
		intent.putExtra("file", file);
		intent.putExtra("type", type);
		intent.putExtra("parentChangeset", parentChangesetId);
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ApiImageLoader<BitbucketService> imageLoader = AppComponentService.obtain(getApplicationContext())
				.apiImageLoader();
		Bundle b = getIntent().getExtras();
		String owner = b.getString("owner");
		String slug = b.getString("slug");
		String changeset_id = b.getString("changeset");
		String file = b.getString("file");
		String parent_changeset_id = b.getString("parentChangeset");
		Type type = (Type) b.getSerializable("type");
		setTitle(slug + ": " + changeset_id);
		setToolbarSubtitle(file);
		toolbar.setNavigationIcon(R.drawable.close);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		if (Helper.isImage(file)) {
			webView.setVisibility(View.GONE);
			diff_image_wrapper.setVisibility(View.VISIBLE);
			Integer numberOfImages = 0;
			if (type == Type.REMOVED || type == Type.MODIFIED) {
				imageLoader.loadImage(this, RawFileStreamRequest.create(owner, slug, parent_changeset_id, file),
						new ImageReadyListener(diff_image_old, numberOfImages));
				diff_image_old.setVisibility(View.VISIBLE);
			}
			if (type == Type.ADDED || type == Type.MODIFIED) {
				imageLoader.loadImage(this, RawFileStreamRequest.create(owner, slug, changeset_id, file),
						new ImageReadyListener(diff_image, numberOfImages));
				diff_image.setVisibility(View.VISIBLE);
			}
		} else {
			showProgressBar(true);
			BitbucketApiCallCompiler.using(spiceManager)
					.apiCall(DiffRequest.builder()
							.username(owner)
							.repoSlug(slug)
							.spec(changeset_id)
							.path(file)
							.build())
					.listener(new DiffRequestListener())
					.alwaysReturned()
					.cacheKeyPrefix("diff")
					.execute();

			webView.setVisibility(View.VISIBLE);
			diff_image_wrapper.setVisibility(View.GONE);
		}
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

	final class DiffRequestListener implements RequestListener<String> {
		@Override
		public void onRequestFailure(SpiceException spiceException) {
			displayDiff("");
		}

		@Override
		public void onRequestSuccess(String result) {
			displayDiff(result);
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void displayDiff(String result) {
		String diff = result.substring(result.indexOf('\n') + 1);
		if (StringUtils.isBlank(diff)) {
			diff = this.getString(R.string.diff_not_found);
		}
		String code = TextUtils.htmlEncode(diff.replace("\t", "    "));
		WebSettings settings = webView.getSettings();
		settings.setBuiltInZoomControls(true);
		settings.setUseWideViewPort(true);
		settings.setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new JavaScriptInterface(code), "bitbeaker");
		webView.setWebChromeClient(new WebChromeClient());
		webView.loadUrl("file:///android_asset/diff.html");

		showProgressBar(false);
	}

	protected class JavaScriptInterface {
		private String code;

		public JavaScriptInterface(String code) {
			this.code = code;
		}

		@JavascriptInterface
		public String getCode() {
			return code;
		}
	}

	final class ImageReadyListener extends SimpleTarget<GlideDrawable> {
		private final ImageView imageView;
		private Integer images;

		ImageReadyListener(ImageView imageView, Integer images) {
			this.imageView = imageView;
			this.images = images;
		}

		@Override
		public void onLoadStarted(Drawable placeholder) {
			++images;
			showProgressBar(true);
		}

		@Override
		public void onLoadFailed(Exception e, Drawable errorDrawable) {
			loadingEnd();
		}

		@Override
		public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
			imageView.setImageDrawable(resource);
			loadingEnd();
		}

		@Override
		public void onLoadCleared(Drawable placeholder) {
			loadingEnd();
		}

		private void loadingEnd() {
			--images;
			if (images <= 0) {
				showProgressBar(false);
			}
		}
	}
}
