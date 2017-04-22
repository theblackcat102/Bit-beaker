package fi.iki.kuitsi.bitbeaker.network;

import android.app.Application;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

/**
 * RSS Service.
 */
public class RssService extends SpiceService {

	@Override
	public CacheManager createCacheManager(Application application) {
		return new CacheManager() {
			@Override
			public <T> T saveDataToCacheAndReturnData(T data, Object cacheKey) throws CacheSavingException, CacheCreationException {
				return data;
			}
		};
	}

	@Override
	protected NetworkStateChecker getNetworkStateChecker() {
		return new ConnectivityChecker();
	}
}
