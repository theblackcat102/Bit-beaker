package fi.iki.kuitsi.bitbeaker.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import static fi.iki.kuitsi.bitbeaker.BuildConfig.APPLICATION_ID;

/**
 * Contract class for interacting with {@link BitbeakerProvider}.
 */
public final class BitbeakerContract {
	private BitbeakerContract() {
		throw new AssertionError("No instance");
	}

	/**
	 * Columns supported by "repository" records.
	 */
	interface RepositoryColumns {
		/** Repository owner. */
		String REPO_OWNER = "repo_owner";
		/** Repo slug. */
		String REPO_SLUG = "repo_slug";
		/** Repository name. */
		String REPO_NAME = "repo_name";
		/** Starred flag. */
		String REPO_STARRED = "repo_starred";
		/** Private flag. */
		String REPO_PRIVATE = "repo_private";

		String REPO_STARRED_ON = "repo_starred_on";
	}

	/**
	 * Columns supported by "event" records.
	 */
	interface EventColumns {
		/** Unique id identifying this event. */
		String EVENT_ID = "event_id";
		String EVENT_TYPE = "event_type";
		String EVENT_CREATED_ON = "event_created_on";
	}

	/**
	 * Columns supported by "user" records.
	 */
	interface UserColumns {
		/** Unique id identifying this user. */
		String USER_ID = "user_id";
		String USER_NAME = "user_name";
		String USER_DISPLAY_NAME = "user_display_name";
		String USER_AVATAR_URL = "user_avatar_url";
	}

	/** Content provider authority. */
	public static final String CONTENT_AUTHORITY = APPLICATION_ID;

	/** Base URI. (content://fi.iki.kuitsi.bitbeaker) */
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	/** Path component for "repository"-type resources. */
	static final String PATH_REPOSITORIES = "repositories";
	static final String PATH_STARRED = "starred";
	static final String PATH_PRIVATE = "private";

	/** Path component for "event"-type resources. */
	static final String PATH_EVENTS = "event";

	/** Path component for "user"-type resources. */
	static final String PATH_USERS = "users";

	/**
	 * Project under revision control.
	 */
	public static class Repository implements RepositoryColumns, BaseColumns {

		/** Fully qualified URI for "repository" resources. */
		public static final Uri CONTENT_URI =
				Uri.withAppendedPath(BASE_CONTENT_URI, PATH_REPOSITORIES);

		public static final Uri CONTENT_STARRED_URI =
				Uri.withAppendedPath(CONTENT_URI, PATH_STARRED);

		public static final Uri CONTENT_PRIVATE_URI =
				Uri.withAppendedPath(CONTENT_URI, PATH_PRIVATE);

		/** MIME type for lists of entries. */
		public static final String CONTENT_TYPE =
				ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.bitbeaker.repository";

		/** MIME type for individual entries. */
		public static final String CONTENT_ITEM_TYPE =
				ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.bitbeaker.repository";

		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT =
				RepositoryColumns.REPO_NAME + " COLLATE LOCALIZED ASC, "
						+ RepositoryColumns.REPO_OWNER + " COLLATE LOCALIZED ASC,"
						+ RepositoryColumns.REPO_SLUG + " COLLATE LOCALIZED ASC";

		/** Build {@link Uri} for requested ID. */
		public static Uri buildRequestUri(Long repositoryId) {
			return CONTENT_URI.buildUpon().appendPath(Long.toString(repositoryId)).build();
		}

		/** Read {@link #_ID} from {@link Repository} {@link Uri}. */
		public static String getRepositoryId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	/**
	 * Events occur on repositories.
	 */
	public static class Events implements EventColumns, BaseColumns {

		/** Fully qualified URI for "event" resources. */
		public static final Uri CONTENT_URI =
				Uri.withAppendedPath(BASE_CONTENT_URI, PATH_EVENTS);

		/** MIME type for lists of entries. */
		public static final String CONTENT_TYPE =
				ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.bitbeaker.repository.event";

		/** MIME type for individual entries. */
		public static final String CONTENT_ITEM_TYPE =
				ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.bitbeaker.repository.event";

		public static final String REPOSITORY_ID = "repository_id";
		public static final String USER_ID = "user_id";

		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT
				= EVENT_CREATED_ON + " DESC";

		/** Build {@link Uri} for requested ID. */
		public static Uri buildRequestUri(int eventId) {
			return CONTENT_URI.buildUpon().appendPath(Integer.toString(eventId)).build();
		}

		/** Read {@link #EVENT_ID} from {@link Events} {@link Uri}. */
		public static String getEventId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	/**
	 * Individual or team account of which activity induce {@link Events}.
	 */
	public static class Users implements UserColumns, BaseColumns {

		/** Fully qualified URI for "event" resources. */
		public static final Uri CONTENT_URI =
				Uri.withAppendedPath(BASE_CONTENT_URI, PATH_USERS);

		/** MIME type for lists of entries. */
		public static final String CONTENT_TYPE =
				ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.bitbeaker.user";

		/** MIME type for individual entries. */
		public static final String CONTENT_ITEM_TYPE =
				ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.bitbeaker.user";

		/** Read {@link #USER_ID} from {@link Users} {@link Uri}. */
		public static String getEventId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}
}
