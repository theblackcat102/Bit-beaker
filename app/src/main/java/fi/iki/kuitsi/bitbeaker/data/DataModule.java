package fi.iki.kuitsi.bitbeaker.data;

import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.octo.android.robospice.networkstate.NetworkStateChecker;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import fi.iki.kuitsi.bitbeaker.data.remote.RemoteModule;
import fi.iki.kuitsi.bitbeaker.network.ConnectivityChecker;

@Module(includes = RemoteModule.class)
public abstract class DataModule {
	@Provides @Singleton static ImageLoader provideImageLoader(OkHttpUrlLoader urlLoader) {
		return new GlideImageLoader(urlLoader);
	}

	@Provides static NetworkStateChecker provideNetworkStateChecker() {
		return new ConnectivityChecker();
	}

	@Binds abstract ImageGetterFactory bindImageGetterFactory(GlideImageGetter.Factory factory);
}
