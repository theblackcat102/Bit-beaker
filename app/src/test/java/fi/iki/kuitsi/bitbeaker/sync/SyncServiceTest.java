package fi.iki.kuitsi.bitbeaker.sync;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.IBinder;
import android.test.mock.MockCursor;

import com.octo.android.robospice.networkstate.NetworkStateChecker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.util.ContentProviderController;

import fi.iki.kuitsi.bitbeaker.BuildConfig;
import fi.iki.kuitsi.bitbeaker.account.Authenticator;
import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;
import fi.iki.kuitsi.bitbeaker.domainobjects.Event;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;
import fi.iki.kuitsi.bitbeaker.domainobjects.User;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.RepositoryEvents;
import fi.iki.kuitsi.bitbeaker.provider.BitbeakerContract;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE, sdk = BuildConfig.ROBOLECTRIC_SDK)
public class SyncServiceTest {

	@Mock(answer = Answers.RETURNS_DEEP_STUBS) BitbucketService bitbucketMock;
	@Mock ContentProvider provider;
	@Mock Cursor emptyCursor;
	@Mock NetworkStateChecker networkStateChecker;
	@Captor ArgumentCaptor<List<SyncHelper.RepositoryEvent>> repositoryEventCaptor;
	private final Account account = new Account("accountName", Authenticator.getAccountType());
	private final Context context = RuntimeEnvironment.application;
	private final ContentResolver contentResolver = RuntimeEnvironment.application.getContentResolver();
	private final NotificationManager notificationManager = (NotificationManager)
			RuntimeEnvironment.application.getSystemService(Context.NOTIFICATION_SERVICE);
	private SyncResult syncResult;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(emptyCursor.moveToNext()).thenReturn(false);
		ContentProviderController.of(provider);
		ShadowContentResolver.registerProviderInternal(BitbeakerContract.CONTENT_AUTHORITY, provider);
		syncResult = new SyncResult();
	}

	@After
	public void tearDown() {
		shadowOf(notificationManager).cancelAll();
	}

	@Test
	public void binder() throws Exception {
		SyncService service = Robolectric.buildService(SyncService.class).create().get();
		ComponentName componentName = new ComponentName(context, SyncService.class);
		Intent serviceIntent = new Intent()
				.setAction("android.content.Sync.Adapter")
				.setComponent(componentName);
		IBinder binder = service.onBind(serviceIntent);
		assertThat(binder).isInstanceOf(Class.forName("android.content.ISyncAdapter"));
	}

	@Test
	public void syncWhenNoNetwork() {
		when(networkStateChecker.isNetworkAvailable(context)).thenReturn(false);

		ContentProviderClient contentProviderClient = mock(ContentProviderClient.class);
		SyncHelper syncHelper = new SyncHelper(context, bitbucketMock, networkStateChecker);
		syncHelper.performSync(account, contentProviderClient, syncResult);

		verifyZeroInteractions(contentProviderClient);

		assertThat(syncResult.stats.numDeletes).isEqualTo(0);
		assertThat(syncResult.stats.numSkippedEntries).isEqualTo(0);
		assertThat(syncResult.stats.numInserts).isEqualTo(0);
	}

	@Test
	public void syncFailedDueToNetworkError() throws Exception {
		when(bitbucketMock.repositoryEvents(anyString(), anyString(), isNull(), isNull(), isNull()).execute())
			.thenThrow(new IOException());

		when(networkStateChecker.isNetworkAvailable(context)).thenReturn(true);

		SyncHelper syncHelper = spy(new SyncHelper(context, bitbucketMock, networkStateChecker));

		doReturn(new RepositoryMockCursor()).when(provider).query(
				eq(BitbeakerContract.Repository.CONTENT_STARRED_URI),
				any(String[].class), ArgumentMatchers.<String>isNull(), ArgumentMatchers.<String[]>isNull(), ArgumentMatchers.<String>isNull());

		doReturn(emptyCursor).when(provider).query(
				eq(BitbeakerContract.Events.CONTENT_URI),
				any(String[].class), ArgumentMatchers.<String>isNull(), ArgumentMatchers.<String[]>isNull(), ArgumentMatchers.<String>isNull());

		doReturn(emptyCursor).when(provider).query(
				eq(BitbeakerContract.Users.CONTENT_URI),
				any(String[].class), ArgumentMatchers.<String>isNull(), ArgumentMatchers.<String[]>isNull(), ArgumentMatchers.<String>isNull());

		ContentProviderClient contentProviderClient = contentResolver.acquireContentProviderClient(BitbeakerContract.CONTENT_AUTHORITY);

		try {
			syncHelper.performSync(account, contentProviderClient, syncResult);
		} finally {
			contentProviderClient.release();
		}

		verify(syncHelper, never()).displayNotification(repositoryEventCaptor.capture());

		Notification notification = shadowOf(notificationManager).getNotification(0);
		assertThat(notification).isNull();

		assertThat(syncResult.stats.numIoExceptions).isEqualTo(1);
	}

	@Test
	public void syncAdapter() throws Exception {
		User user1 = new User("user1");
		User user2 = new User("user2");

		Event event1 = new Event("pushed", user1, new Date());
		Event event2 = new Event("report_issue", user2, new Date());

		List<Event> eventList1 = Collections.singletonList(event1);
		List<Event> eventList2 = Collections.singletonList(event2);

		RepositoryEvents events1 = new RepositoryEvents(eventList1);
		RepositoryEvents events2 = new RepositoryEvents(eventList2);

		when(bitbucketMock.repositoryEvents("owner1", "slug1", null, null, null).execute())
				.thenReturn(Response.success(events1));
		when(bitbucketMock.repositoryEvents("owner2", "slug2", null, null, null).execute())
				.thenReturn(Response.success(events2));

		when(networkStateChecker.isNetworkAvailable(context)).thenReturn(true);

		SyncHelper syncHelper = spy(new SyncHelper(context, bitbucketMock, networkStateChecker));

		doReturn(new RepositoryMockCursor()).when(provider).query(
				eq(BitbeakerContract.Repository.CONTENT_STARRED_URI),
				any(String[].class), ArgumentMatchers.<String>isNull(), ArgumentMatchers.<String[]>isNull(), ArgumentMatchers.<String>isNull());

		doReturn(emptyCursor).when(provider).query(
				eq(BitbeakerContract.Events.CONTENT_URI),
				any(String[].class), ArgumentMatchers.<String>isNull(), ArgumentMatchers.<String[]>isNull(), ArgumentMatchers.<String>isNull());

		doReturn(emptyCursor).when(provider).query(
				eq(BitbeakerContract.Users.CONTENT_URI),
				any(String[].class), ArgumentMatchers.<String>isNull(), ArgumentMatchers.<String[]>isNull(), ArgumentMatchers.<String>isNull());

		ContentProviderClient contentProviderClient = contentResolver.acquireContentProviderClient(BitbeakerContract.CONTENT_AUTHORITY);

		try {
			syncHelper.performSync(account, contentProviderClient, syncResult);
		} finally {
			contentProviderClient.release();
		}

		verify(syncHelper, times(1)).displayNotification(repositoryEventCaptor.capture());
		List<SyncHelper.RepositoryEvent> events = repositoryEventCaptor.getValue();

		assertThat(events).hasSize(2);
		List<String> eventTypes = Arrays.asList(events.get(0).getEventType(),
				events.get(1).getEventType());
		assertThat(eventTypes).containsExactly("pushed", "report_issue");

		Notification notification = shadowOf(notificationManager).getNotification(0);
		assertThat(notification).isNotNull();

		assertThat(syncResult.stats.numDeletes).isEqualTo(0);
		assertThat(syncResult.stats.numSkippedEntries).isEqualTo(0);
		assertThat(syncResult.stats.numInserts).isEqualTo(4); // 2 events + 2 users
	}

	// Mock classes

	private static class RepositoryMockCursor extends MockCursor {

		private final List<Repository> repositories;
		private final AtomicInteger id;
		private int index;

		RepositoryMockCursor() {
			repositories = new ArrayList<>();
			repositories.add(new Repository("owner1", "slug1"));
			repositories.add(new Repository("owner2", "slug2"));
			index = -1;
			id = new AtomicInteger();
		}

		@Override
		public void close() {
		}

		@Override
		public boolean moveToNext() {
			++index;
			return (index < repositories.size());
		}

		@Override
		public int getInt(int columnIndex) {
			return id.getAndIncrement(); // id
		}

		@Override
		public long getLong(int columnIndex) {
			return 0L; // created on
		}

		@Override
		public String getString(int columnIndex) {
			switch (columnIndex) {
				case 1:
					return repositories.get(index).getOwner();
				case 2:
					return repositories.get(index).getSlug();
				case 3:
					return repositories.get(index).getDisplayName();
				default:
					return super.getString(index);
			}
		}
	}
}
