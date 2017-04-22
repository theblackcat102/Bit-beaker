package fi.iki.kuitsi.bitbeaker.data;

import android.content.Context;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import java.io.File;

public interface ImageLoader {
	@UiThread
	void loadImage(Context context, String url, ImageView imageView);
	@UiThread
	void loadAvatar(Context context, String url, ImageView imageView);
	@UiThread
	void loadImage(Fragment fragment, String url, ImageView imageView);
	@WorkerThread
	File getImage(Context context, String url);
}
