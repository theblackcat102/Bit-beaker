package fi.iki.kuitsi.bitbeaker.data.remote;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.concurrent.ExecutionException;

import fi.iki.kuitsi.bitbeaker.data.api.interactor.ApiCall;
import fi.iki.kuitsi.bitbeaker.data.api.ApiImageLoader;
import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;
import okhttp3.ResponseBody;

final class BitbucketGlideImageLoader implements ApiImageLoader<BitbucketService> {

	private final BitbucketService service;

	public BitbucketGlideImageLoader(BitbucketService service) {
		this.service = service;
	}

	@Override
	public void loadImage(Context context, ApiCall<BitbucketService, ResponseBody> apiCall,
			ImageView view) {
		Glide.with(context).using(new RetrofitUrlLoader<>(service)).load(apiCall).into(view);
	}

	@Override
	public void loadImage(Context context, ApiCall<BitbucketService, ResponseBody> apiCall,
			Target<GlideDrawable> target) {
		Glide.with(context).using(new RetrofitUrlLoader<>(service)).load(apiCall).into(target);
	}

	@Override
	public File getImage(Context context, ApiCall<BitbucketService, ResponseBody> apiCall) {
		try {
			return Glide.with(context).using(new RetrofitUrlLoader<>(service)).load(apiCall)
					.downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
					.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
}
