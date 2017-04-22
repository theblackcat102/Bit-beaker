package fi.iki.kuitsi.bitbeaker.activities;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.data.api.model.repositories.DownloadableItem;
import fi.iki.kuitsi.bitbeaker.data.api.model.repositories.DownloadableItems;
import fi.iki.kuitsi.bitbeaker.data.api.interactor.DownloadableItemsRequest;
import fi.iki.kuitsi.bitbeaker.data.api.interactor.FileDownloadRequest;
import fi.iki.kuitsi.bitbeaker.data.remote.BitbucketApiCallCompiler;
import fi.iki.kuitsi.bitbeaker.network.RestService;
import fi.iki.kuitsi.bitbeaker.ui.download.DownloadableItemAdapter;
import okhttp3.ResponseBody;
import retrofit2.Response;

public final class DownloadsActivity extends BaseRepositoryActivity
		implements DownloadableItemAdapter.OnItemClickListener {

	protected static final String TAG = "downloads";

	DownloadableItemAdapter adapter;
	@BindView(R.id.recycler_view) RecyclerView recyclerView;
	private final SpiceManager spiceManager = new SpiceManager(RestService.class);

	public DownloadsActivity() {
		super(R.layout.activity_downloads);
	}

	public static Intent createIntent(Context context, String owner, String slug) {
		Intent intent = new Intent(context, DownloadsActivity.class);
		return BaseRepositoryActivity.addExtendedDataToIntent(intent, owner, slug);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ButterKnife.bind(this);
		adapter = new DownloadableItemAdapter(this);
		recyclerView.setAdapter(adapter);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
	}

	@Override
	public void onStart() {
		super.onStart();
		showProgressBar(true);
		spiceManager.start(this);
		BitbucketApiCallCompiler.using(spiceManager)
				.apiCall(DownloadableItemsRequest.create(getOwner(), getSlug()))
				.listener(new DownloadRequestListener())
				.cacheExpiredOn(1, TimeUnit.HOURS)
				.cacheKeyPrefix("downloads")
				.execute();
	}

	@Override
	public void onStop() {
		spiceManager.shouldStop();
		super.onStop();
	}

	@Override
	public void onItemClick(final DownloadableItem item) {
		Log.d(TAG, String.format("clicked on %s", item));
		BitbucketApiCallCompiler.using(spiceManager)
				.apiCall(new FileDownloadRequest(item))
				.responseListener(new FileDownloadRequestListener(item))
				.execute();
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	void enqueueDownload(DownloadableItem item, Uri uri) {
		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
				== PackageManager.PERMISSION_DENIED) {
			// TODO request permission
			return;
		}

		DownloadManager.Request request = new DownloadManager.Request(uri)
				.setTitle(item.name)
				.setDescription(item.type)
				.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
				.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, item.name);
		request.allowScanningByMediaScanner();

		DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		manager.enqueue(request);
	}

	final class DownloadRequestListener implements RequestListener<DownloadableItems> {
		@Override
		public void onRequestFailure(SpiceException spiceException) {
			showProgressBar(false);
			Log.e(TAG, "failed", spiceException);
		}

		@Override
		public void onRequestSuccess(DownloadableItems downloadableItems) {
			showProgressBar(false);
			adapter.setItems(downloadableItems.getValues());
		}
	}

	final class FileDownloadRequestListener implements RequestListener<Response<ResponseBody>> {
		private final DownloadableItem item;

		public FileDownloadRequestListener(DownloadableItem item) {
			this.item = item;
		}

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			showProgressBar(false);
			Log.e(TAG, "failed", spiceException);
		}

		@Override
		public void onRequestSuccess(Response<ResponseBody> response) {
			showProgressBar(false);
			Uri uri = Uri.parse(response.raw().request().url().toString());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				enqueueDownload(item, uri);
			} else {
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
			}
		}
	}
}
