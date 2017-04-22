package fi.iki.kuitsi.bitbeaker.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Provider that stores {@link BitbeakerContract} data.
 */
public class BitbeakerProvider extends ContentProvider {
	// Used for debugging and logging
	private static final String TAG = "BitbeakerProvider";

	// Handle to a new DatabaseHelper.
	private BitbeakerDatabase dbHelper;

	// URI matcher instance
	private static final UriMatcher sUriMatcher = buildUriMatcher();

	// The incoming URI matches the Favorites URI pattern
	private static final int REPOSITORIES = 100;
	// The incoming URI matches the Favorites ID URI pattern
	private static final int REPOSITORIES_STARRED = 101;
	private static final int REPOSITORIES_PRIVATE = 102;
	private static final int REPOSITORIES_ID = 103;
	private static final int EVENTS = 200;
	private static final int EVENTS_ID = 201;
	private static final int USERS = 300;
	private static final int USERS_ID = 301;

	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = BitbeakerContract.CONTENT_AUTHORITY;
		matcher.addURI(authority, BitbeakerContract.PATH_REPOSITORIES,
				REPOSITORIES);
		matcher.addURI(authority, BitbeakerContract.PATH_REPOSITORIES + "/"
				+ BitbeakerContract.PATH_STARRED, REPOSITORIES_STARRED);
		matcher.addURI(authority, BitbeakerContract.PATH_REPOSITORIES + "/"
				+ BitbeakerContract.PATH_PRIVATE, REPOSITORIES_PRIVATE);
		matcher.addURI(authority, BitbeakerContract.PATH_REPOSITORIES + "/*",
				REPOSITORIES_ID);
		matcher.addURI(authority, BitbeakerContract.PATH_EVENTS,
				EVENTS);
		matcher.addURI(authority, BitbeakerContract.PATH_EVENTS + "/*",
				EVENTS_ID);
		matcher.addURI(authority, BitbeakerContract.PATH_USERS,
				USERS);
		matcher.addURI(authority, BitbeakerContract.PATH_USERS + "/*",
				USERS_ID);
		return matcher;
	}

	@Override
	public boolean onCreate() {
		dbHelper = new BitbeakerDatabase(getContext());

		return true;
	}

	private void deleteDatabase() {
		dbHelper.close();
		Context context = getContext();
		BitbeakerDatabase.deleteDatabase(context);
		dbHelper = new BitbeakerDatabase(context);
	}

	/**
	 * This is called when a client calls {@link android.content.ContentResolver#getType(Uri)}.
	 * Returns the MIME data type of the URI given as a parameter.
	 *
	 * @param uri The URI whose MIME type is desired.
	 * @return The MIME type of the URI.
	 * @throws UnsupportedOperationException if the incoming URI pattern is invalid.
	 */
	@Override
	public String getType(@NonNull Uri uri) {
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case REPOSITORIES:
			case REPOSITORIES_STARRED:
			case REPOSITORIES_PRIVATE:
				return BitbeakerContract.Repository.CONTENT_TYPE;
			case REPOSITORIES_ID:
				return BitbeakerContract.Repository.CONTENT_ITEM_TYPE;
			case EVENTS:
				return BitbeakerContract.Events.CONTENT_TYPE;
			case EVENTS_ID:
				return BitbeakerContract.Events.CONTENT_ITEM_TYPE;
			case USERS:
				return BitbeakerContract.Users.CONTENT_TYPE;
			case USERS_ID:
				return BitbeakerContract.Users.CONTENT_ITEM_TYPE;
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
						String sortOrder) {
		Log.v(TAG, "query(uri=" + uri + ")");
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		final SelectionBuilder builder = buildSelection(uri);
		return builder.where(selection, selectionArgs).query(db, projection, sortOrder);
	}

	/** {@inheritDoc} */
	@Override
	public Uri insert(@NonNull Uri uri, ContentValues values) {
		Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case REPOSITORIES: {
				db.insertOrThrow(BitbeakerDatabase.Tables.REPOSITORIES, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return BitbeakerContract.Repository.CONTENT_URI;
			}
			case EVENTS: {
				db.insertOrThrow(BitbeakerDatabase.Tables.EVENTS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return BitbeakerContract.Events.CONTENT_URI;
			}
			case USERS: {
				db.insertOrThrow(BitbeakerDatabase.Tables.USERS, null, values);
				getContext().getContentResolver().notifyChange(uri, null);
				return BitbeakerContract.Users.CONTENT_URI;
			}
			default: {
				throw new UnsupportedOperationException("Unknown uri: " + uri);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		Log.v(TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSimpleSelection(uri);
		int retVal = builder.where(selection, selectionArgs).update(db, values);
		getContext().getContentResolver().notifyChange(uri, null);
		return retVal;
	}

	/** {@inheritDoc} */
	@Override
	public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
		Log.v(TAG, "delete(uri=" + uri + ")");
		if (uri == BitbeakerContract.BASE_CONTENT_URI) {
			// Handle whole database deletes
			deleteDatabase();
			getContext().getContentResolver().notifyChange(uri, null, false);
			return 1;
		}
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSimpleSelection(uri);
		int retVal = builder.where(selection, selectionArgs).delete(db);
		getContext().getContentResolver().notifyChange(uri, null);
		return retVal;
	}

	/**
	 * Build a simple {@link SelectionBuilder} to match the requested
	 * {@link Uri}.
	 */
	private SelectionBuilder buildSimpleSelection(Uri uri) {
		final SelectionBuilder builder = new SelectionBuilder();
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case REPOSITORIES: {
				return builder.table(BitbeakerDatabase.Tables.REPOSITORIES);
			}
			case REPOSITORIES_STARRED: {
				return builder.table(BitbeakerDatabase.Tables.REPOSITORIES)
						.where(BitbeakerContract.Repository.REPO_STARRED + "=1");
			}
			case REPOSITORIES_PRIVATE: {
				return builder.table(BitbeakerDatabase.Tables.REPOSITORIES)
						.where(BitbeakerContract.Repository.REPO_PRIVATE + "=1");
			}
			case REPOSITORIES_ID: {
				final String repositoryId = BitbeakerContract.Repository.getRepositoryId(uri);
				return builder.table(BitbeakerDatabase.Tables.REPOSITORIES)
						.where(BitbeakerContract.Repository._ID + "=?", repositoryId);
			}
			case EVENTS: {
				return builder.table(BitbeakerDatabase.Tables.EVENTS);
			}
			case EVENTS_ID: {
				final String eventId = BitbeakerContract.Events.getEventId(uri);
				return builder.table(BitbeakerDatabase.Tables.EVENTS)
						.where(BitbeakerContract.Events.EVENT_ID + "=?", eventId);
			}
			case USERS: {
				return builder.table(BitbeakerDatabase.Tables.USERS);
			}
			case USERS_ID: {
				final String userId = BitbeakerContract.Users.getEventId(uri);
				return builder.table(BitbeakerDatabase.Tables.USERS)
						.where(BitbeakerContract.Users.USER_ID + "=?", userId);
			}
			default: {
				throw new UnsupportedOperationException("Unknown uri: " + uri);
			}
		}
	}

	/**
	 * Build a {@link SelectionBuilder} to match the requested {@link Uri} for complex queries.
	 */
	private SelectionBuilder buildSelection(Uri uri) {
		final SelectionBuilder builder = new SelectionBuilder();
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case EVENTS: {
				return builder.table(BitbeakerDatabase.Tables.EVENTS_JOIN_REPOSITORIES_USERS)
						.mapToTable(BitbeakerContract.Events._ID, BitbeakerDatabase.Tables.EVENTS)
						.mapToTable(BitbeakerContract.Events.REPOSITORY_ID,
								BitbeakerDatabase.Tables.EVENTS)
						.mapToTable(BitbeakerContract.Events.USER_ID,
								BitbeakerDatabase.Tables.EVENTS);
			}
			case EVENTS_ID: {
				final String eventId = BitbeakerContract.Events.getEventId(uri);
				return builder.table(BitbeakerDatabase.Tables.EVENTS_JOIN_REPOSITORIES_USERS)
						.mapToTable(BitbeakerContract.Events._ID, BitbeakerDatabase.Tables.EVENTS)
						.mapToTable(BitbeakerContract.Events.REPOSITORY_ID,
								BitbeakerDatabase.Tables.EVENTS)
						.mapToTable(BitbeakerContract.Events.USER_ID,
								BitbeakerDatabase.Tables.EVENTS)
						.where(BitbeakerContract.Events.EVENT_ID + "=?", eventId);
			}
			default:
				return buildSimpleSelection(uri);
		}
	}
}
