package fi.iki.kuitsi.bitbeaker;

import com.octo.android.robospice.SpiceManager;

import dagger.Subcomponent;
import fi.iki.kuitsi.bitbeaker.di.scope.ActivityScoped;

@ActivityScoped
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {
	SpiceManager spiceManager();

	@Subcomponent.Builder
	interface Builder {
		ActivityComponent build();
	}
}
