package fi.iki.kuitsi.bitbeaker.data.api;

import android.content.Context;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import fi.iki.kuitsi.bitbeaker.data.api.interactor.ApiCall;
import okhttp3.ResponseBody;

public interface ApiImageLoader<S> {
	@UiThread
	void loadImage(Context context, ApiCall<S, ResponseBody> apiCall, ImageView view);
	@UiThread
	void loadImage(Context context, ApiCall<S, ResponseBody> apiCall, Target<GlideDrawable> target);
	@WorkerThread
	File getImage(Context context, ApiCall<S, ResponseBody> apiCall);
}
