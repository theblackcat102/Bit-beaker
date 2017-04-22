package fi.iki.kuitsi.bitbeaker.data.api;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module
public class ApiModule {
	@Provides @Singleton static Gson provideGson() {
		return new GsonBuilder()
				.setDateFormat("yyyy-MM-dd HH:mm:ssZ")
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
				.create();
	}

	@Provides @Singleton static LoginService provideLoginService(@Named("non-authenticated") OkHttpClient client, Gson gson) {
		Retrofit retrofit = new Retrofit.Builder()
				.addConverterFactory(GsonConverterFactory.create(gson))
				.baseUrl("https://bitbucket.org/site/oauth2/")
				.client(client)
				.build();
		return retrofit.create(LoginService.class);
	}

	@Provides @Singleton static Retrofit provideRetrofit(@Named("authenticated") OkHttpClient client, Gson gson) {
		return new Retrofit.Builder()
				.addCallAdapterFactory(SpiceCallAdapter.FACTORY)
				.addConverterFactory(ScalarsConverterFactory.create())
				.addConverterFactory(GsonConverterFactory.create(gson))
				.baseUrl(BitbucketService.BASE_URL)
				.client(client)
				.build();
	}

	@Provides @Singleton static BitbucketService provideBitbucketService(Retrofit retrofit) {
		return retrofit.create(BitbucketService.class);
	}
}
