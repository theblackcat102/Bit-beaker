package fi.iki.kuitsi.bitbeaker.sync;

import android.accounts.Account;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.octo.android.robospice.networkstate.NetworkStateChecker;

import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.activities.RepositoriesActivity;
import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;
import fi.iki.kuitsi.bitbeaker.domainobjects.Event;
import fi.iki.kuitsi.bitbeaker.domainobjects.User;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.RepositoryEvents;
import fi.iki.kuitsi.bitbeaker.provider.BitbeakerContract;
import fi.iki.kuitsi.bitbeaker.util.ResourceUtils;
import retrofit2.Response;
import fi.iki.kuitsi.bitbeaker.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

/**
 * Helper class used by {@link SyncAdapter}.
 */
public class SyncHelper {

	private static final String TAG = "SyncHelper";
	private static final String GROUP_KEY_EVENTS = "group_key_events";
	private static final Comparator<RepositoryEvent> EVENT_CREATED_ON_COMPARATOR =
			new Comparator<RepositoryEvent>() {
				public int compare(RepositoryEvent lhs, RepositoryEvent rhs) {
					return lhs.getCreationDate().compareTo(rhs.getCreationDate());
				}
			};

	private final Context context;
	private final BitbucketService service;
	private final NetworkStateChecker networkStateChecker;

	@Inject
	public SyncHelper(Context context, BitbucketService service, NetworkStateChecker networkStateChecker) {
		this.context = context;
		this.service = service;
		this.networkStateChecker = networkStateChecker;
	}

	/**
	 * Perform a sync.
	 */
	public void performSync(Account account, ContentProviderClient provider,
			SyncResult syncResult) {

		final long startTime = System.currentTimeMillis();

		if (!networkStateChecker.isNetworkAvailable(context)) {
			Log.d(TAG, "Skip sync - offline");
			return;
		}

		try {
			List<StarredRepository> repositories = getStarredRepositories(provider);
			if (repositories.isEmpty()) {
				Log.d(TAG, "Skip sync - no starred repositories");
				return;
			}

			Set<Integer> localEventIdSet = getEventIDs(provider);
			Set<Integer> localUserIdSet = Collections.unmodifiableSet(getUserIDs(provider));
			Set<Integer> remoteEventIdSet = new HashSet<>();

			ArrayList<ContentProviderOperation> batch = new ArrayList<>();

			List<RepositoryEvent> newEvents = new ArrayList<>();

			for (StarredRepository repository : repositories) {
				Log.d(TAG, "Get events of " + repository.owner + "/" + repository.slug);

				final Response<RepositoryEvents> response = service.repositoryEvents(repository.owner,
						repository.slug, null, null, null).execute();
				if (!response.isSuccessful()) throw new IOException();
				final Event.List events = response.body().events;

				for (Event event : events) {
					final int eventId = event.hashCode();

					remoteEventIdSet.add(eventId);

					// Skip synced events.
					if (localEventIdSet.contains(eventId)) {
						continue;
					}

					final User user = event.getUser();

					if (user == null) {
						continue;
					}

					if (!localUserIdSet.contains(user.hashCode())) {
						syncResult.stats.numInserts++;
						SyncHelper.addUser(user, batch);
					}

					syncResult.stats.numInserts++;
					SyncHelper.addEvent(event, repository, user, batch);

					if (!account.name.equals(event.getUser().getUsername())
						&& event.getCreationDate().after(repository.starredOn)) {
						newEvents.add(new RepositoryEvent(event, repository));
					} else {
						syncResult.stats.numSkippedEntries++;
					}
				}
			}

			localEventIdSet.removeAll(remoteEventIdSet);
			for (int id : localEventIdSet) {
				syncResult.stats.numDeletes++;
				batch.add(ContentProviderOperation.newDelete(
						BitbeakerContract.Events.buildRequestUri(id)).build());
			}

			Log.d(TAG, "No. new events: " + syncResult.stats.numInserts);
			Log.d(TAG, "No. deleted events: " + syncResult.stats.numDeletes);

			Log.v(TAG, "Merge solution ready. Applying batch update");
			provider.applyBatch(batch);

			displayNotification(newEvents);
		} catch (IOException e) {
			syncResult.stats.numIoExceptions++;
		} catch (RemoteException | OperationApplicationException e) {
			syncResult.databaseError = true;
		} finally {
			Log.i(TAG, "Update took " + (System.currentTimeMillis() - startTime) + " ms");
		}
	}

	private interface RepositoriesQuery {
		String[] PROJECTION = new String[] {
			BitbeakerContract.Repository._ID,
			BitbeakerContract.Repository.REPO_OWNER,
			BitbeakerContract.Repository.REPO_SLUG,
			BitbeakerContract.Repository.REPO_NAME,
			BitbeakerContract.Repository.REPO_STARRED_ON
		};

		int COLUMN_ID = 0;
		int COLUMN_OWNER = 1;
		int COLUMN_SLUG = 2;
		int COLUMN_NAME = 3;
		int COLUMN_STARRED_ON = 4;
	}

	private List<StarredRepository> getStarredRepositories(ContentProviderClient provider) throws
			RemoteException {
		List<StarredRepository> starredRepositories = new ArrayList<>();
		Cursor c = provider.query(BitbeakerContract.Repository.CONTENT_STARRED_URI,
				RepositoriesQuery.PROJECTION, null, null, null);
		assert c != null;
		while (c.moveToNext()) {
			StarredRepository sr = new StarredRepository(
					c.getInt(RepositoriesQuery.COLUMN_ID),
					c.getString(RepositoriesQuery.COLUMN_OWNER),
					c.getString(RepositoriesQuery.COLUMN_SLUG),
					c.getString(RepositoriesQuery.COLUMN_NAME),
					new Date(c.getLong(RepositoriesQuery.COLUMN_STARRED_ON)));
			starredRepositories.add(sr);
		}
		c.close();

		return Collections.unmodifiableList(starredRepositories);
	}

	private Set<Integer> getEventIDs(ContentProviderClient provider) throws RemoteException {
		Set<Integer> ids = new HashSet<>();
		Cursor c = provider.query(BitbeakerContract.Events.CONTENT_URI,
				new String[]{BitbeakerContract.Events.EVENT_ID}, null, null, null);
		assert c != null;
		while (c.moveToNext()) {
			ids.add(c.getInt(0));
		}
		c.close();
		return ids;
	}

	private Set<Integer> getUserIDs(ContentProviderClient provider) throws RemoteException {
		Set<Integer> ids = new HashSet<>();
		Cursor c = provider.query(BitbeakerContract.Users.CONTENT_URI,
				new String[]{BitbeakerContract.Users.USER_ID}, null, null, null);
		assert c != null;
		while (c.moveToNext()) {
			ids.add(c.getInt(0));
		}
		c.close();
		return ids;
	}

	private static void addUser(User user, ArrayList<ContentProviderOperation> batch) {
		batch.add(ContentProviderOperation.newInsert(BitbeakerContract.Users.CONTENT_URI)
				.withValue(BitbeakerContract.Users.USER_ID, user.hashCode())
				.withValue(BitbeakerContract.Users.USER_NAME, user.getUsername())
				.withValue(BitbeakerContract.Users.USER_DISPLAY_NAME, user.getDisplayName())
				.withValue(BitbeakerContract.Users.USER_AVATAR_URL, user.getAvatarUrl())
				.build());
	}

	private static void addEvent(Event event, StarredRepository repository, User user,
			ArrayList<ContentProviderOperation> batch) {
		batch.add(ContentProviderOperation.newInsert(BitbeakerContract.Events.CONTENT_URI)
				.withValue(BitbeakerContract.Events.EVENT_ID, event.hashCode())
				.withValue(BitbeakerContract.Events.EVENT_TYPE, event.getEventType())
				.withValue(BitbeakerContract.Events.EVENT_CREATED_ON,
						event.getCreationDate().getTime())
				.withValue(BitbeakerContract.Events.REPOSITORY_ID, repository.id)
				.withValue(BitbeakerContract.Events.USER_ID, user.hashCode())
				.build());
	}

	/**
	 * Build and display a notification.
	 *
	 * Display one event
	 * Ticker: repository name
	 * Content title: New event
	 * Content text: repository name
	 * Time: event's timestamp
	 *
	 * Display more events (N is the number of new events)
	 * Ticker: N new events
	 * Content title: New events
	 * Content info: N
	 * Content text: comma separated list of repositories
	 * Time: latest event's timestamp
	 *
	 * Expanded notification (Inbox-style notification; API level 16)
	 * User name + event type
	 *
	 * @param events Notification is build from these events.
	 */
	void displayNotification(List<RepositoryEvent> events) {
		if (events.isEmpty()) return;

		Collections.sort(events, Collections.reverseOrder(EVENT_CREATED_ON_COMPARATOR));

		Intent intent = new Intent(context, RepositoriesActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		final Resources res = context.getResources();
		final String title = res.getQuantityString(R.plurals.notification_title, events.size());
		final Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
		final RepositoryEvent latestEvent = events.get(0);

		final NotificationCompat.Builder notificationBuilder =
				new NotificationCompat.Builder(context)
						.setAutoCancel(true)
						.setContentIntent(pendingIntent)
						.setContentTitle(title)
						.setGroup(GROUP_KEY_EVENTS)
						.setLargeIcon(largeIcon)
						.setPriority(NotificationCompat.PRIORITY_DEFAULT)
						.setSmallIcon(R.drawable.ic_stat_notify)
						.setWhen(latestEvent.getCreationDate().getTime());

		if (events.size() == 1) {
			final String repositoryName = latestEvent.getRepository().owner + "/"
					+ latestEvent.getRepository().slug;
			notificationBuilder
					.setContentText(repositoryName)
					.setTicker(repositoryName);
		} else {
			final NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
					.setBigContentTitle(title);

			final Locale locale = ResourceUtils.getCurrentLocale(context);
			final Set<StarredRepository> repositories = new HashSet<>();
			for (RepositoryEvent event : events) {
				String eventText = translateApiString(event.getEventType());
				inboxStyle.addLine(event.getUser().getDisplayName() + " "
						+ eventText.toLowerCase(locale));
				repositories.add(event.getRepository());
			}

			final String ticker = res.getQuantityString(R.plurals.notification_number,
					events.size(), events.size());
			final String summary = buildRepositoryList(repositories);

			inboxStyle.setSummaryText(summary);

			notificationBuilder
					.setContentInfo(Integer.toString(events.size()))
					.setContentText(summary)
					.setGroupSummary(true)
					.setStyle(inboxStyle)
					.setTicker(ticker);
		}

		NotificationManagerCompat.from(context).notify(0, notificationBuilder.build());
	}

	private String translateApiString(String eventType) {
		String translatedString = ResourceUtils.getResourceStringValue(context, "event_type_" + eventType);
		if (StringUtils.isNotBlank(translatedString)) {
			return translatedString;
		}
		return eventType;
	}

	private String buildRepositoryList(Iterable<StarredRepository> repositories) {
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for (StarredRepository repository : repositories) {
			if (firstTime) {
				firstTime = false;
			} else {
				sb.append(", ");
			}
			sb.append(repository.owner);
			sb.append('/');
			sb.append(repository.name);
		}
		return sb.toString();
	}

	private static class StarredRepository {
		final long id;
		final String owner;
		final String slug;
		final String name;
		final Date starredOn;

		StarredRepository(long id, String owner, String slug, String name, Date starredOn) {
			this.id = id;
			this.owner = owner;
			this.slug = slug;
			this.name = name;
			this.starredOn = starredOn;
		}
	}

	protected static class RepositoryEvent {
		final Event event;
		final StarredRepository repository;

		RepositoryEvent(Event event, StarredRepository repository) {
			this.event = event;
			this.repository = repository;
		}

		public String getEventType() {
			return event.getEventType();
		}

		public User getUser() {
			return event.getUser();
		}

		public Date getCreationDate() {
			return event.getCreationDate();
		}

		public StarredRepository getRepository() {
			return repository;
		}
	}
}
