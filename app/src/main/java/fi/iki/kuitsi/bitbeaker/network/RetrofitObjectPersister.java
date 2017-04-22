package fi.iki.kuitsi.bitbeaker.network;

import android.app.Application;

import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Converter;

/**
 * InFileObjectPersister for Retrofit responses.
 */
final class RetrofitObjectPersister<T> extends InFileObjectPersister<T> {

	private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

	private final Converter<ResponseBody, T> responseConverter;
	private final Converter<T, RequestBody> requestConverter;


	RetrofitObjectPersister(Application application, Class<T> clazz, File cacheFolder,
			Converter<ResponseBody, T> responseConverter,
			Converter<T, RequestBody> requestConverter)
			throws CacheCreationException {
		super(application, clazz, cacheFolder);
		this.responseConverter = responseConverter;
		this.requestConverter = requestConverter;
	}

	@Override
	protected T readCacheDataFromFile(File file) throws CacheLoadingException {
		BufferedSource fileSource = null;
		try {
			fileSource = Okio.buffer(Okio.source(file));
			ResponseBody responseBody = ResponseBody.create(JSON_MEDIA_TYPE,
					fileSource.readLong(),
					fileSource);
			return responseConverter.convert(responseBody);
		} catch (FileNotFoundException e) {
			// Should not occur (we test before if file exists)
			// Do not throw, file is not cached
			//Ln.w("file " + file.getAbsolutePath() + " does not exists", e);
			return null;
		} catch (Exception e) {
			throw new CacheLoadingException(e);
		} finally {
			if (fileSource != null) {
				try {
					fileSource.close();
				} catch (IOException ignore) {
					// ignore
				}
			}
		}
	}

	@Override
	public T saveDataToCacheAndReturnData(final T data, final Object cacheKey)
			throws CacheSavingException {
		try {
			if (isAsyncSaveEnabled()) {
				Thread t = new Thread() {
					@Override
					public void run() {
						try {
							saveData(data, cacheKey);
						} catch (IOException ignore) {
							// ignore
						}
					}
				};
				t.start();
			} else {
				saveData(data, cacheKey);
			}
		} catch (Exception e) {
			throw new CacheSavingException(e);
		}
		return data;
	}

	private void saveData(T data, Object cacheKey) throws IOException {
		// transform the content in json to store it in the cache
		RequestBody requestBody = requestConverter.convert(data);
		BufferedSink fileSink = Okio.buffer(Okio.sink(getCacheFile(cacheKey)));
		try {
			fileSink.writeLong(requestBody.contentLength());
			requestBody.writeTo(fileSink);
		} finally {
			fileSink.close();
		}
	}
}
