package fi.iki.kuitsi.bitbeaker.data.remote;

import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import fi.iki.kuitsi.bitbeaker.account.AuthenticatedUserManager;
import fi.iki.kuitsi.bitbeaker.data.api.ApiImageLoader;
import fi.iki.kuitsi.bitbeaker.data.api.ApiModule;
import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;
import fi.iki.kuitsi.bitbeaker.data.api.LoginService;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@Module(includes = ApiModule.class)
public final class RemoteModule {

	@Provides @Singleton static OkHttpClient.Builder provideOkHttpClientBuilder() {
		return new OkHttpClient.Builder()
				.connectTimeout(15, TimeUnit.SECONDS)
				.readTimeout(20, TimeUnit.SECONDS)
				.writeTimeout(20, TimeUnit.SECONDS);
	}

	@Provides @Singleton @Named("non-authenticated")
	static OkHttpClient providerOkHttpClient(OkHttpClient.Builder builder) {
		return builder.build();
	}

	@Provides @Singleton @Named ("authenticated")
	static OkHttpClient providerAuthenticatedOkHttpClient(OkHttpClient.Builder builder, LoginService loginService, AuthenticatedUserManager userManager) {
		builder.addInterceptor(new AuthenticationInterceptor(userManager));
		builder.authenticator(new RefreshTokenAuthenticator(loginService, userManager));
		return builder.build();
	}

	@Provides @Singleton
	static OkHttpUrlLoader provideOkHttpUrlLoader(
			final @Named ("non-authenticated") OkHttpClient client,
			final @Named ("authenticated") OkHttpClient authenticatedClient) {
		return new OkHttpUrlLoader(new Call.Factory() {
			@Override
			public Call newCall(Request request) {
				if (request.url().toString().startsWith(BitbucketService.BASE_URL) || request.url().toString().startsWith("http://bitbucket.org")) {
					return authenticatedClient.newCall(request);
				}
				return client.newCall(request);
			}
		});
	}

	@Provides @Singleton static ApiImageLoader<BitbucketService> provideApiImageLoader(BitbucketService service) {
		return new BitbucketGlideImageLoader(service);
	}
}
