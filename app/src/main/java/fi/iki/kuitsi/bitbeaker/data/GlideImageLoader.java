package fi.iki.kuitsi.bitbeaker.data;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelCache;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.HttpUrl;

final class GlideImageLoader implements ImageLoader {

	private final BaseGlideUrlLoader<String> urlLoader;
	private final BaseGlideUrlLoader<String> avatarUrlLoader;

	GlideImageLoader(final ModelLoader<GlideUrl, InputStream> modelLoader) {
		urlLoader = new BaseGlideUrlLoader<String>(modelLoader) {
			@Override
			protected String getUrl(String model, int width, int height) {
				return model;
			}
		};
		ModelCache<String, GlideUrl> avatarUrlCache = new ModelCache<>();
		avatarUrlLoader = new BaseGlideUrlLoader<String>(modelLoader, avatarUrlCache) {
			@Override
			protected String getUrl(String model, int width, int height) {
				return rewriteAvatarUrl(model, Math.max(width, height));
			}
		};
	}

	@Override
	public void loadImage(Context context, String url, ImageView imageView) {
		getDrawableTypeRequest(Glide.with(context), url).into(imageView);
	}

	@Override
	public void loadAvatar(Context context, String url, ImageView imageView) {
		Glide.with(context).using(avatarUrlLoader).load(url).into(imageView);
	}

	@Override
	public void loadImage(Fragment fragment, String url, ImageView imageView) {
		getDrawableTypeRequest(Glide.with(fragment), url).into(imageView);
	}

	@Override
	public File getImage(Context context, String url) {
		try {
			return getDrawableTypeRequest(Glide.with(context), url)
					.downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
					.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	static String rewriteAvatarUrl(String url, int size) {
		HttpUrl httpUrl = HttpUrl.parse(url);
		List<String> pathSegments = httpUrl.pathSegments();
		if (pathSegments.contains("avatar") && pathSegments.size() > 3) {
			return httpUrl.newBuilder()
					.removePathSegment(3)
					.addPathSegment(Integer.toString(size))
					.toString();
		}
		return url;
	}

	private DrawableTypeRequest<String> getDrawableTypeRequest(RequestManager requestManager, String url) {
		return requestManager.using(urlLoader).load(url);
	}
}
