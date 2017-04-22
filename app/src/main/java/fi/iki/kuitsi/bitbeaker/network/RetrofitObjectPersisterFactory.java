package fi.iki.kuitsi.bitbeaker.network;

import android.app.Application;

import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;
import com.octo.android.robospice.persistence.file.InFileObjectPersisterFactory;

import java.io.File;
import java.lang.annotation.Annotation;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * A factory that will create {@link RetrofitObjectPersister} instances that save/load JSON data in
 * a file.
 */
final class RetrofitObjectPersisterFactory extends InFileObjectPersisterFactory {

	private static final Annotation[] EMPTY_ANNOTATION = new Annotation[0];
	private final Retrofit retrofit;

	RetrofitObjectPersisterFactory(Application application, Retrofit retrofit)
			throws CacheCreationException {
		super(application);
		setCachePrefix("");
		this.retrofit = retrofit;
	}

	@Override
	public <T> InFileObjectPersister<T> createInFileObjectPersister(Class<T> clazz,
			File cacheFolder) throws CacheCreationException {
		final Converter<ResponseBody, T> responseConverter = retrofit.responseBodyConverter(clazz, EMPTY_ANNOTATION);
		final Converter<T, RequestBody> requestConverter = retrofit.requestBodyConverter(clazz, EMPTY_ANNOTATION, EMPTY_ANNOTATION);
		return new RetrofitObjectPersister<>(getApplication(), clazz, cacheFolder, responseConverter, requestConverter);
	}
}
