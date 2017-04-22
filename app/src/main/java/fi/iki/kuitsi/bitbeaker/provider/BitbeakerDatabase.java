package fi.iki.kuitsi.bitbeaker.provider;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import fi.iki.kuitsi.bitbeaker.Bitbeaker;
import fi.iki.kuitsi.bitbeaker.provider.BitbeakerContract.Events;
import fi.iki.kuitsi.bitbeaker.provider.BitbeakerContract.EventColumns;
import fi.iki.kuitsi.bitbeaker.provider.BitbeakerContract.Repository;
import fi.iki.kuitsi.bitbeaker.provider.BitbeakerContract.RepositoryColumns;
import fi.iki.kuitsi.bitbeaker.provider.BitbeakerContract.Users;
import fi.iki.kuitsi.bitbeaker.provider.BitbeakerContract.UserColumns;

/**
 * Helper for managing {@link SQLiteDatabase} that stores data for
 * {@link BitbeakerProvider}. This class helps open, create, and upgrade
 * the database file.
 */
public class BitbeakerDatabase extends SQLiteOpenHelper {

	/**
	 * Debug tag for use logging debug output to LogCat.
	 */
	private static final String TAG = "BitbeakerDatabase";

	/**
	 * The database that the provider uses as its underlying data store.
	 */
	private static final String DATABASE_NAME = "bitbeaker.db";

	/**
	 * The database version.
	 */
	private static final int DATABASE_VERSION = 1;

	/**
	 * Fully-qualified field names.
	 */
	private interface Qualified {
		String EVENTS_REPOSITORY_ID = Tables.EVENTS + "." + Events.REPOSITORY_ID;
		String EVENTS_USER_ID = Tables.EVENTS + "." + Events.USER_ID;
		String REPOSITORIES_ID = Tables.REPOSITORIES + "." + Repository._ID;
		String USERS_USER_ID = Tables.USERS + "." + Users.USER_ID;
	}

	/* package */ interface Tables {

		String REPOSITORIES = "repositories";
		String EVENTS = "events";
		String USERS = "users";

		String EVENTS_JOIN_REPOSITORIES_USERS = Tables.EVENTS
				+ " LEFT OUTER JOIN " + Tables.REPOSITORIES + " ON "
				+ Qualified.EVENTS_REPOSITORY_ID + "=" + Qualified.REPOSITORIES_ID
				+ " LEFT OUTER JOIN " + Tables.USERS + " ON " + Qualified.EVENTS_USER_ID + "="
				+ Qualified.USERS_USER_ID;
	}

	private interface Triggers {
		// Deletes from events when corresponding repositories are deleted.
		String REPOSITORIES_EVENTS_DELETE = "repositories_events_delete";
		// Deletes from users
		String EVENTS_USERS_DELETE = "events_users_delete";
	}

	/**
	 * {@code REFERENCES} clauses.
	 */
	private interface References {
		String REPOSITORY_ID = "REFERENCES " + Tables.REPOSITORIES + "(" + Repository._ID + ")";
		String USER_ID = "REFERENCES " + Tables.USERS + "(" + Users.USER_ID + ")";
	}

	private static final String COMMA_SEP = ",";

	/**
	 * SQL statement to create "repository" table.
	 */
	private static final String SQL_CREATE_REPOSITORIES =
			"CREATE TABLE " + Tables.REPOSITORIES + " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP
					+ RepositoryColumns.REPO_OWNER + " TEXT" + COMMA_SEP
					+ RepositoryColumns.REPO_SLUG + " TEXT" + COMMA_SEP
					+ RepositoryColumns.REPO_NAME + " TEXT" + COMMA_SEP
					+ RepositoryColumns.REPO_STARRED + " INTEGER NOT NULL DEFAULT 0" + COMMA_SEP
					+ RepositoryColumns.REPO_PRIVATE + " INTEGER NOT NULL DEFAULT 0" + COMMA_SEP
					+ RepositoryColumns.REPO_STARRED_ON + " INTEGER NOT NULL DEFAULT 0)";

	/**
	 * SQL statement to drop "repository" table.
	 */
	private static final String SQL_DELETE_REPOSITORIES =
			"DROP TABLE IF EXISTS " + Tables.REPOSITORIES;

	/**
	 * SQL statement to create {@link Triggers#REPOSITORIES_EVENTS_DELETE} trigger.
	 */
	private static final String SQL_CREATE_DELETE_REPOSITORY_TRIGGER =
			"CREATE TRIGGER " + Triggers.REPOSITORIES_EVENTS_DELETE + " AFTER DELETE ON "
					+ Tables.REPOSITORIES + " BEGIN DELETE FROM " + Tables.EVENTS
					+ " WHERE " + Qualified.EVENTS_REPOSITORY_ID + "=old." + Repository._ID + ";"
					+ " END;";

	/**
	 * SQL statement to drop {@link Triggers#REPOSITORIES_EVENTS_DELETE} trigger.
	 */
	private static final String SQL_DELETE_DELETE_REPOSITORY_TRIGGER =
			"DROP TRIGGER IF EXISTS " + Triggers.REPOSITORIES_EVENTS_DELETE;

	/**
	 * SQL statement to create "events" table.
	 */
	private static final String SQL_CREATE_EVENTS =
			"CREATE TABLE " + Tables.EVENTS + " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP
					+ EventColumns.EVENT_ID + " INTEGER NOT NULL" + COMMA_SEP
					+ Events.REPOSITORY_ID + " INTEGER " + References.REPOSITORY_ID + COMMA_SEP
					+ Events.USER_ID + " INTEGER " + References.USER_ID + COMMA_SEP
					+ EventColumns.EVENT_TYPE + " TEXT NOT NULL" + COMMA_SEP
					+ EventColumns.EVENT_CREATED_ON + " INTEGER NOT NULL" + COMMA_SEP
					+ "UNIQUE (" + EventColumns.EVENT_ID + ") ON CONFLICT REPLACE" + ")";

	/**
	 * SQL statement to drop "events" table.
	 */
	private static final String SQL_DELETE_EVENTS = "DROP TABLE IF EXISTS " + Tables.EVENTS;

	/**
	 * SQL statement to create {@link Triggers#EVENTS_USERS_DELETE} trigger.
	 */
	private static final String SQL_CREATE_DELETE_EVENT_TRIGGER =
			"CREATE TRIGGER " + Triggers.EVENTS_USERS_DELETE + " AFTER DELETE ON "
					+ Tables.EVENTS + " WHEN ((SELECT COUNT() FROM " + Tables.EVENTS + " WHERE "
					+ Events.USER_ID + "=old." + Events.USER_ID + ") = 0)"
					+ "BEGIN DELETE FROM " + Tables.USERS + " WHERE " + Users.USER_ID + "=old."
					+ Events.USER_ID + "; END;";

	/**
	 * SQL statement to drop {@link Triggers#EVENTS_USERS_DELETE} trigger.
	 */
	private static final String SQL_DELETE_DELETE_EVENT_TRIGGER =
			"DROP TRIGGER IF EXISTS " + Triggers.EVENTS_USERS_DELETE;

	/**
	 * SQL statement to create "users" table.
	 */
	private static final String SQL_CREATE_USERS =
			"CREATE TABLE " + Tables.USERS + " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP
					+ UserColumns.USER_ID + " INTEGER NOT NULL" + COMMA_SEP
					+ UserColumns.USER_NAME + " TEXT" + COMMA_SEP
					+ UserColumns.USER_DISPLAY_NAME + " TEXT" + COMMA_SEP
					+ UserColumns.USER_AVATAR_URL + " TEXT" + COMMA_SEP
					+ "UNIQUE (" + UserColumns.USER_ID + ") ON CONFLICT REPLACE" + ")";

	/**
	 * SQL statement to drop "users" table.
	 */
	private static final String SQL_DELETE_USERS = "DROP TABLE IF EXISTS " + Tables.USERS;

	private final Context context;

	public BitbeakerDatabase(Context context) {
		// calls the super constructor, requesting the default cursor factory.
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	/**
	 * Creates the underlying database with table name and column names taken from the
	 * {@link BitbeakerContract} class.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_REPOSITORIES);
		db.execSQL(SQL_CREATE_DELETE_REPOSITORY_TRIGGER);
		db.execSQL(SQL_CREATE_EVENTS);
		db.execSQL(SQL_CREATE_DELETE_EVENT_TRIGGER);
		db.execSQL(SQL_CREATE_USERS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
		Log.w(TAG, "Destroying old data during upgrade");
		cancelSync();
		reCreateDb(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onDowngrade() from " + oldVersion + " to " + newVersion);
		Log.w(TAG, "Destroying old data during downgrade");
		cancelSync();
		reCreateDb(db);
	}

	private void cancelSync() {
		// Cancel any sync currently in progress
		Account account = Bitbeaker.get(context).getAccount();
		if (account != null) {
			Log.i(TAG, "Cancelling any pending syncs for for account");
			ContentResolver.cancelSync(account, BitbeakerContract.CONTENT_AUTHORITY);
		}
	}

	private void reCreateDb(SQLiteDatabase db) {
		db.execSQL(SQL_DELETE_REPOSITORIES);
		db.execSQL(SQL_DELETE_DELETE_REPOSITORY_TRIGGER);
		db.execSQL(SQL_DELETE_EVENTS);
		db.execSQL(SQL_DELETE_DELETE_EVENT_TRIGGER);
		db.execSQL(SQL_DELETE_USERS);
		onCreate(db);
	}

	static void deleteDatabase(Context context) {
		context.deleteDatabase(DATABASE_NAME);
	}
}
