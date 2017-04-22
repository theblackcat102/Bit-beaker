package fi.iki.kuitsi.bitbeaker.data;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;
import com.bumptech.glide.load.model.stream.StreamModelLoader;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import fi.iki.kuitsi.bitbeaker.R;

/**
 * Glide as {@link Html.ImageGetter}.
 *
 * <a href="https://github.com/bumptech/glide/issues/550">Glide #550</a>
 */
final class GlideImageGetter implements Html.ImageGetter, Drawable.Callback {

	private static final String TAG = GlideImageLoader.class.getSimpleName();

	private final StreamModelLoader<String> modelLoader;
	private final Context context;
	private final TextView textView;
	private final Set<ImageGetterViewTarget> targets;

	GlideImageGetter(StreamModelLoader<String> modelLoader, Context context, TextView textView) {
		this.modelLoader = modelLoader;
		this.context = context;
		this.textView = textView;

		clear(); // Cancel all previous request
		targets = new HashSet<>();
		this.textView.setTag(R.id.text_view_drawable_callback, this);
	}

	static GlideImageGetter get(View view) {
		return (GlideImageGetter) view.getTag(R.id.text_view_drawable_callback);
	}

	@Override
	public Drawable getDrawable(String url) {
		final UrlDrawable urlDrawable = new UrlDrawable();

		Log.d(TAG, "Downloading from: " + url);
		Glide.with(context).using(modelLoader).load(url)
				.into(new ImageGetterViewTarget(textView, urlDrawable));

		return urlDrawable;
	}

	@Override
	public void invalidateDrawable(Drawable who) {
		textView.invalidate();
	}

	@Override
	public void scheduleDrawable(Drawable who, Runnable what, long when) {

	}

	@Override
	public void unscheduleDrawable(Drawable who, Runnable what) {

	}

	void clear() {
		GlideImageGetter prev = get(textView);
		if (prev == null) return;

		for (ImageGetterViewTarget target : prev.targets) {
			Log.d(TAG, "Cleared!");
			Glide.clear(target);
		}
	}

	final class ImageGetterViewTarget extends ViewTarget<TextView, GlideDrawable> {

		private final UrlDrawable drawable;
		private Request request;

		ImageGetterViewTarget(TextView view, UrlDrawable urlDrawable) {
			super(view);
			targets.add(this); // Add ViewTarget into Set
			this.drawable = urlDrawable;
		}

		@Override
		public void onLoadFailed(Exception e, Drawable errorDrawable) {
			Log.e(TAG, "failed to load ", e);
		}

		@Override
		public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
			// Resize images - Scale image proportionally to fit TextView width
			float width;
			float height;
			if (resource.getIntrinsicWidth() >= getView().getWidth()) {
				float downScale = (float) resource.getIntrinsicWidth() / getView().getWidth();
				width = (float) resource.getIntrinsicWidth() / downScale;
				height = (float) resource.getIntrinsicHeight() / downScale;
			} else {
				float multiplier = (float) getView().getWidth() / resource.getIntrinsicWidth();
				width = (float) resource.getIntrinsicWidth() * multiplier;
				height = (float) resource.getIntrinsicHeight() * multiplier;
			}
			Rect rect = new Rect(0, 0, Math.round(width), Math.round(height));

			resource.setBounds(rect);

			drawable.setBounds(rect);
			drawable.setDrawable(resource);

			if (resource.isAnimated()) {
				// set callback to drawable in order to
				// signal its container to be redrawn
				// to show the animated GIF

				drawable.setCallback(get(getView()));
				resource.setLoopCount(GlideDrawable.LOOP_FOREVER);
				resource.start();
			}

			getView().setText(getView().getText());
			getView().invalidate();
		}

		@Override
		public Request getRequest() {
			return request;
		}

		@Override
		public void setRequest(Request request) {
			this.request = request;
		}
	}

	static final class Factory implements ImageGetterFactory {

		private final BaseGlideUrlLoader<String> modelLoader;

		@Inject Factory(final OkHttpUrlLoader modelLoader) {
			this.modelLoader = new BaseGlideUrlLoader<String>(modelLoader) {
				@Override
				protected String getUrl(String model, int width, int height) {
					return model;
				}
			};
		}

		@Override
		public Html.ImageGetter create(Context context, TextView textView) {
			return new GlideImageGetter(modelLoader, context, textView);
		}
	}

	static final class UrlDrawable extends Drawable implements Drawable.Callback {

		private GlideDrawable drawable;

		@Override
		public void draw(Canvas canvas) {
			if (drawable != null) {
				drawable.draw(canvas);
			}
		}

		@Override
		public void setAlpha(int alpha) {
			if (drawable != null) {
				drawable.setAlpha(alpha);
			}
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			if (drawable != null) {
				drawable.setColorFilter(cf);
			}
		}

		@Override
		public int getOpacity() {
			if (drawable != null) {
				return drawable.getOpacity();
			}
			return PixelFormat.UNKNOWN;
		}

		@Override
		public void invalidateDrawable(Drawable who) {
			if (getCallback() != null) {
				getCallback().invalidateDrawable(who);
			}
		}

		@Override
		public void scheduleDrawable(Drawable who, Runnable what, long when) {
			if (getCallback() != null) {
				getCallback().scheduleDrawable(who, what, when);
			}
		}

		@Override
		public void unscheduleDrawable(Drawable who, Runnable what) {
			if (getCallback() != null) {
				getCallback().unscheduleDrawable(who, what);
			}
		}

		public void setDrawable(GlideDrawable drawable) {
			if (this.drawable != null) {
				this.drawable.setCallback(null);
			}
			drawable.setCallback(this);
			this.drawable = drawable;
		}
	}
}
