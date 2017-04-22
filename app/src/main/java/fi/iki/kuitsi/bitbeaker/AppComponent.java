package fi.iki.kuitsi.bitbeaker;

import javax.inject.Singleton;

import dagger.Component;
import fi.iki.kuitsi.bitbeaker.account.AuthenticatedUserManager;
import fi.iki.kuitsi.bitbeaker.data.ImageGetterFactory;
import fi.iki.kuitsi.bitbeaker.data.ImageLoader;
import fi.iki.kuitsi.bitbeaker.data.api.ApiImageLoader;
import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;
import fi.iki.kuitsi.bitbeaker.data.api.LoginService;
import fi.iki.kuitsi.bitbeaker.data.remote.BitbucketRequestComponent;
import fi.iki.kuitsi.bitbeaker.network.RestService;
import fi.iki.kuitsi.bitbeaker.provider.ProviderModule;
import fi.iki.kuitsi.bitbeaker.sync.SyncComponent;

@Component(modules = {AppModule.class, ProviderModule.class})
@Singleton
public interface AppComponent {
	void inject(RestService service);

	AuthenticatedUserManager userManager();
	FavoritesService favoriteService();
	ImageLoader imageLoader();
	ImageGetterFactory imageGetterFactory();
	ApiImageLoader<BitbucketService> apiImageLoader();
	LoginService loginService();
	BitbucketService bitbucketService();

	ActivityComponent.Builder activityComponentBuilder();
	SyncComponent syncComponent();
	BitbucketRequestComponent.Builder bitbucketRequestComponentBuilder();
}
