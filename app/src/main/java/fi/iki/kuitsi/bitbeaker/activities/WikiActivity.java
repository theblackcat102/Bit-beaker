package fi.iki.kuitsi.bitbeaker.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import butterknife.BindView;
import fi.iki.kuitsi.bitbeaker.Bitbeaker;
import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.network.RestService;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.WikiPageRequest;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.WikiPage;

import java.util.Stack;

public class WikiActivity extends BaseRepositoryActivity {

	private static final int ID_ITEM_COPY_URL = 1;

	@BindView(R.id.wiki_webview) WebView webView;
	private final SpiceManager spiceManager = new SpiceManager(RestService.class);
	private WikiPageRequest wikiPageRequest;
	private String data;
	private String markup;
	private String wikiTemplate = "file:///android_asset/wiki.html";
	private String wikiBaseUrl;
	private String currentSubFolder = "";

	/**
	 * Contains history of API calls in case we need to get previous page by pressing Back button.
	 * Every API call points to assets/wiki.html so normal history of WebView does not work.
	 */
	private Stack<String> apiHistory;

	public WikiActivity() {
		super(R.layout.wiki);
	}

	/**
	 * Create an intent for this activity.
	 *
	 * @param path path to file inside wiki, use <code>null</code> for wiki main page.
	 */
	public static Intent createIntent(Context context, String owner, String slug, String path) {
		Intent intent = new Intent(context, WikiActivity.class);
		intent = BaseRepositoryActivity.addExtendedDataToIntent(intent, owner, slug);
		intent.putExtra("path", path);
		return intent;
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getIntent().getExtras();
		setTitle(getString(R.string.wiki));
		getSupportActionBar().setSubtitle(getOwner() + "/" + getSlug());
		apiHistory = new Stack<>();

		WebSettings settings = webView.getSettings();
		settings.setBuiltInZoomControls(true);
		settings.setUseWideViewPort(true);
		settings.setJavaScriptEnabled(true);
		settings.setUserAgentString(Bitbeaker.getUserAgentString());
		webView.addJavascriptInterface(new JavaScriptInterface(), "bitbeaker");
		webView.setWebChromeClient(new WebChromeClient());
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.startsWith(MyActivity.API_BASE_URL)) {
					// let the Activity handle loading of wiki page via API call
					currentSubFolder = getCurrentSubFolder(url);
					wikiPageRequest.setUrl(url);
					performRequest();
					return true;
				} else if (url.startsWith(wikiTemplate)) {
					// This should be a link to anchor inside a wiki page.
					// Do not override it; let the WebView load the page.
					return false;
				} else {
					// This might not be a safe URL to open as we have exposed our app
					// via JavaScriptInterface. Launch another Activity that handles the URL.
					// http://developer.android.com/guide/webapps/webview.html
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(intent);
					return true;
				}
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				showProgressBar(true);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);

				// load correct markup for normal Wiki pages (loaded from API)
				if (url.equals(wikiTemplate)) {
					view.loadUrl("javascript:updateDropdownMenu();");
					view.pageUp(true);
				}

				showProgressBar(false);
			}
		});

		wikiBaseUrl = MyActivity.API_BASE_URL + "/repositories/" + getOwner() + "/" + getSlug()
				+ "/wiki/";
		String path = b.getString("path", "");
		wikiPageRequest = new WikiPageRequest(getOwner(), getSlug(), path);
	}

	@Override
	public void onStart() {
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
		menu.add(Menu.NONE, ID_ITEM_COPY_URL, Menu.NONE, R.string.repository_copy_url);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == ID_ITEM_COPY_URL) {
			if (Helper.copyStringToClipboard(getWebUrl())) {
				showToast(R.string.repository_url_copied);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);

	}

	/**
	 * Get URL of wiki page. On repositories with private wiki users still need to authenticate
	 * to Bitbucket before they can use the url.
	 *
	 * @return web url of current wiki page
	 */
	private String getWebUrl() {
		return "https://bitbucket.org/" + getOwner() + "/" + getSlug() + "/wiki/"
				+ apiHistory.peek();
	}

	private String getCurrentSubFolder(String url) {
		try {
			url = url.substring(wikiBaseUrl.length(), url.lastIndexOf('/') + 1);
		} catch (IndexOutOfBoundsException e) {
			url = "";
		}
		return url;
	}

	/**
	 * Add URL of API query to top of history stack and fetch data from API.
	 */
	private void performRequest() {
		showProgressBar(true);

		apiHistory.push(wikiPageRequest.getPage());
		spiceManager.execute(wikiPageRequest, wikiPageRequest.getCacheKey(),
				wikiPageRequest.getCacheExpireDuration(), new WikiPageRequestListener());
	}

	@Override
	public void onBackPressed() {
		if (webView.isFocused()) {
			if (webView.canGoBack()) {
				// this works only for external urls, not for API calls
				webView.goBack();
				return;
			} else if (apiHistory.size() > 1) {
				String page = apiHistory.pop();// remove item at top of stack since we are currently on that page
				page = apiHistory.pop();// now get the url of previous page
				wikiPageRequest.setPage(page);
				performRequest();
				return;
			}
		}
		super.onBackPressed();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// no other actions needed: we just need to prevent Activity from
		// restarting on orientation change to retain WebView's history
	}

	private class WikiPageRequestListener implements RequestListener<WikiPage> {
		@Override
		public void onRequestFailure(SpiceException spiceException) {
			data = "Unexpected data from Bitbucket API!";
			markup = "null";
			webView.loadUrl(wikiTemplate);
		}

		@Override
		public void onRequestSuccess(WikiPage wikiPage) {
			data = wikiPage.data;
			markup = wikiPage.markup;
			webView.loadUrl(wikiTemplate);
		}
	}

	protected class JavaScriptInterface {
		@JavascriptInterface
		public String getData() {
			return data;
		}

		@JavascriptInterface
		public String getMarkup() {
			return markup;
		}

		@JavascriptInterface
		public String getWikiHomeUrl() {
			return wikiBaseUrl;
		}

		@JavascriptInterface
		public String getSubFolder() {
			return currentSubFolder;
		}
	}
}
