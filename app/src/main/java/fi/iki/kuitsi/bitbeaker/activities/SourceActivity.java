package fi.iki.kuitsi.bitbeaker.activities;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import de.keyboardsurfer.android.widget.crouton.Style;
import fi.iki.kuitsi.bitbeaker.AppComponentService;
import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.data.api.ApiImageLoader;
import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;
import fi.iki.kuitsi.bitbeaker.data.api.interactor.RawFileStreamRequest;
import fi.iki.kuitsi.bitbeaker.data.remote.BitbucketApiCallCompiler;
import fi.iki.kuitsi.bitbeaker.network.RestService;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.RawContentRequest;

public final class SourceActivity extends BaseActivity {

	private static final String TAG = SourceActivity.class.getSimpleName();
	private static final String PDF_MIME_TYPE = "application/pdf";

	@BindView(R.id.source_image) ImageView imageView;
	@BindView(R.id.source_webview) WebView webView;

	private String slug;
	private String branchOrTag;
	private String owner;
	/** Full path to file, starting with repository root <code>/</code>. */
	private String file;
	private String code;
	private RawFileStreamRequest imageFileStreamRequest;
	private ApiImageLoader<BitbucketService> imageLoader;
	private final SpiceManager spiceManager = new SpiceManager(RestService.class);

	public SourceActivity() {
		super(R.layout.source);
	}

	/**
	 * @param revision Branch, tag or commit hash
	 */
	public static Intent createIntent(Context context, String owner, String slug, String revision,
			String file) {
		final Intent intent = new Intent(context, SourceActivity.class);
		intent.putExtra("owner", owner);
		intent.putExtra("slug", slug);
		intent.putExtra("branchOrTag", revision);
		intent.putExtra("file", file);
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Uri data = getIntent().getData();
		if (data != null) {
			List<String> segments = data.getPathSegments();
			if (segments.size() >= 5 && (segments.get(2).equalsIgnoreCase("src") || segments.get(2).equalsIgnoreCase("raw"))) {
				String filename = "";
				for (int i = 4; i < segments.size(); i++) {
					filename = filename + "/" + segments.get(i);
				}
				Intent intent = SourceActivity.createIntent(this, segments.get(0), segments.get(1), segments.get(3), filename);
				finish();
				startActivity(intent);
				return;
			}
		}

		imageLoader = AppComponentService.obtain(getApplicationContext()).apiImageLoader();
		Bundle b = getIntent().getExtras();
		branchOrTag = b.getString("branchOrTag");
		slug = b.getString("slug");
		owner = b.getString("owner");

		file = b.getString("file");
		if (!file.startsWith("/")) {
			file = "/" + file;
		}

		setTitle(slug + " (" + branchOrTag + ")");
		setToolbarSubtitle(file);
		toolbar.setNavigationIcon(R.drawable.close);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		if (Helper.isImage(file)) {
			imageView.setVisibility(View.VISIBLE);
			imageFileStreamRequest = RawFileStreamRequest.create(owner, slug, branchOrTag, file);
			imageLoader.loadImage(this, imageFileStreamRequest, imageView);
		} else if (file.toLowerCase(Locale.US).endsWith(".pdf")) {
			File outDir = getOutputDirectory();
			File dlFile = new File(outDir, file);
			BitbucketApiCallCompiler.using(spiceManager)
					.apiCall(RawFileStreamRequest.create(owner, slug, branchOrTag, file))
					.downloadListener(new RawStreamRequestListener(PDF_MIME_TYPE))
					.download(dlFile);
			showProgressBar(true);
		} else {
			RawContentRequest request = new RawContentRequest(owner, slug, branchOrTag, file);
			spiceManager.execute(request, request.getCacheKey(), request.getCacheExpireDuration(),
				new RawContentRequestListener());
			showProgressBar(true);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem share = menu.add(Menu.NONE, R.id.action_share, Menu.NONE, R.string.share);
		share.setIcon(R.drawable.share_variant);
		MenuItemCompat.setShowAsAction(share, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.action_share:
				share();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void displayFileContent(String result) {
		code = result;
		webView.setVisibility(View.VISIBLE);
		WebSettings settings = webView.getSettings();
		settings.setBuiltInZoomControls(true);
		settings.setUseWideViewPort(true);
		settings.setJavaScriptEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView.addJavascriptInterface(new JavaScriptInterface(), "bitbeaker");
		webView.setWebChromeClient(new MyWebChromeClient());
		webView.loadUrl("file:///android_asset/source.html");
	}

	private File getOutputDirectory() {
		File outDir = new File(getExternalCacheDir() + File.separator + "download"
				+ File.separator + owner + File.separator + slug);
		if (!outDir.isDirectory()) {
			outDir.mkdirs();
		}
		return outDir;
	}

	private void share() {
		if (Helper.isImage(file)) {
			new GetImageFileFromCache().execute(imageFileStreamRequest);
		} else {
			if (code == null) {
				showToast(R.string.please_wait_still_loading);
				return;
			}
			File outDir = getOutputDirectory();
			String fileName = (branchOrTag + file).replace('/', '_');
			File dlFile = new File(outDir, fileName);
			try {
				FileUtils.writeStringToFile(dlFile, code);
				showChooser(dlFile, "text/*");
			} catch (IOException e) {
				showToast(String.format(this.getString(R.string.unable_to_write_external),
						e.getMessage()));
				e.printStackTrace();
			}
		}
	}

	void showChooser(File file, String mimeType) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		Uri data;
		try {
			data = FileProvider.getUriForFile(this, getString(R.string.share_authority), file);
		} catch (IllegalArgumentException e) {
			showToast(e.getMessage(), Style.ALERT);
			return;
		}
		intent.setDataAndType(data, mimeType);
		try {
			startActivity(Intent.createChooser(intent, null));
		} catch (ActivityNotFoundException ignore) {
			Log.d(TAG, "No application available to view " + intent);
		}
	}

	final class MyWebChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int progress) {
			if (progress < 100) {
				showProgressBar(true);
			} else {
				showProgressBar(false);
			}
		}
	}

	protected class JavaScriptInterface {
		@JavascriptInterface
		public String getCode() {
			return TextUtils.htmlEncode(code.replace("\t", "    "));
		}

		@JavascriptInterface
		public String getRawCode() {
			return code;
		}

		@JavascriptInterface
		public String getFilename() {
			return file;
		}
	}

	final class RawContentRequestListener implements RequestListener<String> {
		@Override
		public void onRequestFailure(SpiceException spiceException) {
			showProgressBar(false);
		}

		@Override
		public void onRequestSuccess(String fileContent) {
			showProgressBar(false);
			displayFileContent(fileContent);
		}
	}

	final class RawStreamRequestListener implements RequestListener<File> {
		private final String mimeType;

		RawStreamRequestListener(String mimeType) {
			this.mimeType = mimeType;
		}

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			showProgressBar(false);
		}

		@Override
		public void onRequestSuccess(File file) {
			showProgressBar(false);
			showChooser(file, mimeType);
			finish();
		}
	}

	final class GetImageFileFromCache extends AsyncTask<RawFileStreamRequest, Void, File> {
		@Override
		protected File doInBackground(RawFileStreamRequest... params) {
			return imageLoader.getImage(SourceActivity.this, params[0]);
		}

		@Override
		protected void onPostExecute(File image) {
			if (image != null) {
				showChooser(image, "image/*");
			} else {
				showToast(R.string.please_wait_still_loading);
			}
		}
	}
}
