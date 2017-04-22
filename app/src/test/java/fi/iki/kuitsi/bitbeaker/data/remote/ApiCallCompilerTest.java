package fi.iki.kuitsi.bitbeaker.data.remote;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.concurrent.TimeUnit;

import fi.iki.kuitsi.bitbeaker.data.api.interactor.ApiCall;
import retrofit2.Call;
import retrofit2.Response;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class ApiCallCompilerTest {

	@Mock SpiceManager spiceManager;
	@Mock RequestListener<TestResponse> listener;
	@Mock RequestListener<Response<TestResponse>> responseListener;
	@Mock RequestListener<File> downloadListener;
	@Mock File file;
	@Captor ArgumentCaptor<SpiceRequest<TestResponse>> executeNonCachedRequestCaptor;
	@Captor ArgumentCaptor<CachedSpiceRequest<TestResponse>> executeRequestCaptor;
	@Captor ArgumentCaptor<RequestListener<TestResponse>> executeRequestListenerCaptor;

	@Before
	public void setUp()  {
		MockitoAnnotations.initMocks(this);
	}

	@Test public void executeRequestThatAlwaysExpired() {
		ApiCallCompiler.<TestService>using(spiceManager)
				.apiCall(new TestApiCall())
				.listener(listener)
				.alwaysExpired()
				.execute();
		verify(spiceManager, atLeastOnce()).execute(executeRequestCaptor.capture(),
				executeRequestListenerCaptor.capture());
		assertThat(executeRequestListenerCaptor.getValue()).isEqualTo(listener);
		assertThat(executeRequestCaptor.getValue().getCacheDuration()).isEqualTo(-1);
		assertThat(executeRequestCaptor.getValue().getRequestCacheKey()).isNull();
	}

	@Test public void executeRequestThatAlwaysReturned() {
		ApiCallCompiler.<TestService>using(spiceManager)
				.apiCall(new TestApiCall())
				.listener(listener)
				.alwaysReturned()
				.cacheKeyPrefix("pref")
				.execute();
		verify(spiceManager, atLeastOnce()).execute(executeRequestCaptor.capture(),
				executeRequestListenerCaptor.capture());
		assertThat(executeRequestListenerCaptor.getValue()).isEqualTo(listener);
		assertThat(executeRequestCaptor.getValue().getCacheDuration()).isEqualTo(0);
		assertThat(executeRequestCaptor.getValue().getRequestCacheKey().toString()).startsWith("pref");
	}

	@Test public void executeRequestWithTimeout() {
		ApiCallCompiler.<TestService>using(spiceManager)
				.apiCall(new TestApiCall())
				.listener(listener)
				.cacheExpiredOn(1, TimeUnit.MINUTES)
				.cacheKeyPrefix("pref")
				.execute();
		verify(spiceManager, atLeastOnce()).execute(executeRequestCaptor.capture(),
				executeRequestListenerCaptor.capture());
		assertThat(executeRequestListenerCaptor.getValue()).isEqualTo(listener);
		assertThat(executeRequestCaptor.getValue().getCacheDuration()).isEqualTo(60000);
		assertThat(executeRequestCaptor.getValue().getRequestCacheKey().toString()).startsWith("pref");
	}

	@Test public void executeRequestWithResponseListener() {
		ApiCallCompiler.<TestService>using(spiceManager)
				.apiCall(new TestApiCall())
				.responseListener(responseListener)
				.execute();
		verify(spiceManager, atLeastOnce()).execute(executeRequestCaptor.capture(),
				executeRequestListenerCaptor.capture());
		assertThat(executeRequestListenerCaptor.getValue()).isEqualTo(responseListener);
	}

	@Test public void downloadRequest() {
		ApiCallCompiler.<TestService>using(spiceManager)
				.apiCall(new TestApiCall())
				.downloadListener(downloadListener)
				.download(file);
		verify(spiceManager, atLeastOnce()).execute(executeNonCachedRequestCaptor.capture(),
				executeRequestListenerCaptor.capture());
		assertThat(executeRequestListenerCaptor.getValue()).isEqualTo(downloadListener);
	}

	class TestService {

	}

	class TestResponse {

	}

	class TestApiCall implements ApiCall<TestService, TestResponse> {

		@Override
		public Class<TestResponse> responseType() {
			return TestResponse.class;
		}

		@Override
		public Call<TestResponse> call(TestService service) {
			return null;
		}
	}
}
