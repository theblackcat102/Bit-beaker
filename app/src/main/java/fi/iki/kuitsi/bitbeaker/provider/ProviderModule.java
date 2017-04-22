package fi.iki.kuitsi.bitbeaker.provider;

import android.app.Application;
import android.content.ContentResolver;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import fi.iki.kuitsi.bitbeaker.FavoritesService;

@Module
public final class ProviderModule {

	@Provides @Singleton static ContentResolver provideContentResolver(Application app) {
		return app.getContentResolver();
	}

	@Provides static QueryHandler provideQueryHandler(ContentResolver contentResolver) {
		return new SimpleAsyncQueryHandler(contentResolver);
	}

	@Provides @Singleton
	static FavoritesService provideFavoritesService(ContentResolver cr, Provider<QueryHandler> qh) {
		return new FavoritesProvider(cr, qh);
	}
}
