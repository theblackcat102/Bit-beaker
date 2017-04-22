package fi.iki.kuitsi.bitbeaker;

import com.octo.android.robospice.SpiceManager;

import dagger.Module;
import dagger.Provides;
import fi.iki.kuitsi.bitbeaker.network.RestService;

@Module
public class ActivityModule {
	@Provides public SpiceManager provideSpiceManager() {
		return new SpiceManager(RestService.class);
	}
}
