package fi.iki.kuitsi.bitbeaker.network;

import android.app.Application;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.ObjectPersisterFactory;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.string.InFileStringObjectPersister;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import fi.iki.kuitsi.bitbeaker.AppComponent;
import fi.iki.kuitsi.bitbeaker.AppComponentService;
import fi.iki.kuitsi.bitbeaker.data.remote.BitbucketRequestComponent;
import fi.iki.kuitsi.bitbeaker.data.remote.VisitableRequest;
import retrofit2.Retrofit;

/**
 * Robospice-Retrofit-Gson REST service.
 */
public class RestService extends SpiceService {

	private static final int THREAD_COUNT = 2;

	@Inject Retrofit retrofit;
	@Inject Provider<NetworkStateChecker> networkStateCheckerProvider;
	@Inject BitbucketRequestComponent.Builder requestComponentBuilder;

	@Override
	public void addRequest(CachedSpiceRequest<?> request,
			Set<RequestListener<?>> listRequestListener) {
		SpiceRequest spiceRequest = request.getSpiceRequest();
		if (spiceRequest instanceof VisitableRequest) {
			((VisitableRequest) spiceRequest).accept(requestComponentBuilder.build());
		}
		super.addRequest(request, listRequestListener);
	}

	@Override
	public CacheManager createCacheManager(Application application) throws CacheCreationException {
		AppComponent appComponent = AppComponentService.obtain(application);
		appComponent.inject(this);
		final CacheManager cacheManager = new CacheManager();
		final ObjectPersisterFactory persisterFactory =
				new RetrofitObjectPersisterFactory(application, retrofit);
		persisterFactory.setAsyncSaveEnabled(true);
		cacheManager.addPersister(new InFileStringObjectPersister(application));
		cacheManager.addPersister(persisterFactory);
		return cacheManager;
	}

	@Override
	protected NetworkStateChecker getNetworkStateChecker() {
		return networkStateCheckerProvider.get();
	}

	@Override
	public int getThreadCount() {
		return THREAD_COUNT;
	}
}
