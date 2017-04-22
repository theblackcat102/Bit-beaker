package fi.iki.kuitsi.bitbeaker;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import fi.iki.kuitsi.bitbeaker.account.AuthenticatedUserManager;
import fi.iki.kuitsi.bitbeaker.data.DataModule;

@Module(includes = DataModule.class)
public class AppModule {

	private final Bitbeaker app;

	AppModule(Bitbeaker app) {
		this.app = app;
	}

	@Provides @Singleton Application provideApplication() {
		return app;
	}

	@Provides @Singleton Context provideApplicationContext() {
		return app;
	}

	@Provides @Singleton AuthenticatedUserManager provideUserManager() {
		AccountManager accountManager = AccountManager.get(app);
		return new AuthenticatedUserManager(accountManager);
	}
}
