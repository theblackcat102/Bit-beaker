package fi.iki.kuitsi.bitbeaker.data.remote;

import com.octo.android.robospice.retry.RetryPolicy;

import dagger.Module;
import dagger.Provides;
import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;
import fi.iki.kuitsi.bitbeaker.di.scope.RequestScoped;

@Module
final class BitbucketRequestModule {
	@Provides @RequestScoped(BitbucketService.class) static RetryPolicy provideRetryPolicy() {
		return new BitbucketRetryPolicy();
	}

}
