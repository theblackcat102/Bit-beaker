package fi.iki.kuitsi.bitbeaker.data.remote;

import dagger.Subcomponent;
import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;
import fi.iki.kuitsi.bitbeaker.di.scope.RequestScoped;

@RequestScoped(BitbucketService.class)
@Subcomponent(modules = BitbucketRequestModule.class)
public interface BitbucketRequestComponent extends RequestComponent<BitbucketService> {
	@Subcomponent.Builder
	interface Builder {
		BitbucketRequestComponent build();
	}
}
