package fi.iki.kuitsi.bitbeaker.provider;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.util.Log;

import fi.iki.kuitsi.bitbeaker.AppComponentService;
import fi.iki.kuitsi.bitbeaker.FavoritesService;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Provider;

/**
 * This class handles the operations related to saving and loading
 * favorite repositories from the {@link BitbeakerProvider}.
 */
public class FavoritesProvider implements FavoritesService {

	private static final String TAG = "FavoritesProvider";

	private final ContentResolver contentResolver;
	private final Provider<QueryHandler> queryHandlerProvider;

	public FavoritesProvider(ContentResolver contentResolver, Provider<QueryHandler> provider) {
		this.contentResolver = contentResolver;
		this.queryHandlerProvider = provider;
	}

	public static FavoritesService getInstance(Context context) {
		return AppComponentService.obtain(context.getApplicationContext()).favoriteService();
	}

	public static ContentValues domainObjectToContentValues(Repository repository) {
		ContentValues values = new ContentValues();
		values.put(BitbeakerContract.Repository.REPO_OWNER, repository.getOwner());
		values.put(BitbeakerContract.Repository.REPO_SLUG, repository.getSlug());
		values.put(BitbeakerContract.Repository.REPO_NAME, repository.getDisplayName());
		values.put(BitbeakerContract.Repository.REPO_STARRED, 1);
		values.put(BitbeakerContract.Repository.REPO_PRIVATE, repository.isPrivateRepository());
		values.put(BitbeakerContract.Repository.REPO_STARRED_ON, new Date().getTime());
		return values;
	}

	public static Repository cursorToDomainObject(Cursor cursor) {
		final String owner = cursor.getString(FavoritesQuery.REPO_OWNER);
		final String slug = cursor.getString(FavoritesQuery.REPO_SLUG);
		final String name = cursor.getString(FavoritesQuery.REPO_NAME);
		final boolean isPrivate = cursor.getInt(FavoritesQuery.REPO_PRIVATE) != 0;

		return new Repository(owner, slug)
				.name(name)
				.privateRepository(isPrivate);
	}

	private static void addRepositoryToBatch(Repository repository,
			ArrayList<ContentProviderOperation> batch) {
		batch.add(ContentProviderOperation.newInsert(BitbeakerContract.Repository.CONTENT_URI)
				.withValues(domainObjectToContentValues(repository))
				.build());
	}

	/**
	 * Get a list of favorite repositories. For test purposes.
	 * Do not call it from the UI thread, use the Android's Loader manager instead.
	 *
	 * @return The list of favorite repositories.
	 */
	@Override
	public List<Repository> getFavorites() {
		List<Repository> repositoryList = new ArrayList<>();
		Cursor cursor = contentResolver.query(
				BitbeakerContract.Repository.CONTENT_STARRED_URI,
				FavoritesQuery.PROJECTION,
				null, null,
				BitbeakerContract.Repository.DEFAULT_SORT);
		while (cursor.moveToNext()) {
			Repository repository = cursorToDomainObject(cursor);
			repositoryList.add(repository);
		}
		cursor.close();
		return repositoryList;
	}

	/**
	 * Saves a repository as a favorite to {@link BitbeakerProvider}.
	 *
	 * @param repository Repository to save
	 */
	@Override
	public void saveFavoriteRepository(Repository repository) {
		queryHandlerProvider.get().startInsert(-1, null,
				BitbeakerContract.Repository.CONTENT_URI,
				domainObjectToContentValues(repository));
	}

	/**
	 * Removes the given repository from the {@link BitbeakerProvider}.
	 *
	 * @param repository Repository to remove
	 */
	@Override
	public void removeFavoriteRepository(Repository repository) {
		String selection = BitbeakerContract.RepositoryColumns.REPO_OWNER + "=? AND "
				+ BitbeakerContract.Repository.REPO_SLUG + "=?";
		String[] selectionArgs = new String[]{repository.getOwner(), repository.getSlug()};

		queryHandlerProvider.get().startDelete(-1, null,
				BitbeakerContract.Repository.CONTENT_STARRED_URI,
				selection, selectionArgs);
	}

	/**
	 * Check whether a repository is a favorite one or not.
	 *
	 * @param repository Repository to check.
	 * @return True if the repository is in the favorites.
	 */
	@Override
	public boolean isFavoriteRepository(Repository repository) {
		boolean retVal = false;

		final String selection = BitbeakerContract.RepositoryColumns.REPO_OWNER + "=? AND "
				+ BitbeakerContract.Repository.REPO_SLUG + "=?";
		final String[] selectionArgs = new String[]{repository.getOwner(), repository.getSlug()};

		Cursor cursor = contentResolver.query(
				BitbeakerContract.Repository.CONTENT_STARRED_URI,
				FavoritesQuery.PROJECTION,
				selection,
				selectionArgs, null);
		if (cursor.moveToFirst()) {
			retVal = true;
		}
		cursor.close();
		return retVal;
	}

	/**
	 * Empties the list of favorite repositories.
	 */
	@Override
	public void deleteFavorites() {
		queryHandlerProvider.get().startDelete(-1, null,
				BitbeakerContract.Repository.CONTENT_STARRED_URI,
				null, null);
	}

	/**
	 * Saves a collection of repositories as a favorite to {@link BitbeakerProvider}. Synchronous
	 * method, do not call it from the UI thread.
	 *
	 * @param repositories Repositories to save.
	 * @return Number of insertions, or -1 if an error occurred.
	 */
	@Override
	public int addFavorites(Collection<Repository> repositories) {
		ArrayList<ContentProviderOperation> batch = new ArrayList<>();
		List<Repository> favorites = getFavorites();
		for (Repository repository : repositories) {
			if (!favorites.contains(repository)) {
				favorites.add(repository);
				addRepositoryToBatch(repository, batch);
				Log.d(TAG, "Add repository: " + repository.getOwner() + "/" + repository.getSlug());
			}
		}

		if (batch.isEmpty()) {
			return 0;
		}

		try {
			contentResolver.applyBatch(BitbeakerContract.CONTENT_AUTHORITY, batch);
		} catch (RemoteException e) {
			Log.e(TAG, "failed to add repositories", e);
			return -1;
		} catch (OperationApplicationException e) {
			Log.e(TAG, "failed to add repositories", e);
			return -1;
		}
		return batch.size();
	}

	interface FavoritesQuery {
		String[] PROJECTION = {
				BaseColumns._ID,
				BitbeakerContract.Repository.REPO_OWNER,
				BitbeakerContract.Repository.REPO_SLUG,
				BitbeakerContract.Repository.REPO_NAME,
				BitbeakerContract.Repository.REPO_PRIVATE,
		};

		int _ID = 0;
		int REPO_OWNER = 1;
		int REPO_SLUG = 2;
		int REPO_NAME = 3;
		int REPO_PRIVATE = 4;
	}
}
