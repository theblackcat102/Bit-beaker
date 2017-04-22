package fi.iki.kuitsi.bitbeaker.provider;

import android.content.Context;
import android.support.v4.content.CursorLoader;

/**
 * {@link CursorLoader} for Favorite Repositories.
 */
public class FavoritesLoader extends CursorLoader {

	public FavoritesLoader(Context context) {
		super(context, BitbeakerContract.Repository.CONTENT_STARRED_URI,
				FavoritesProvider.FavoritesQuery.PROJECTION,
				null, null,
				BitbeakerContract.Repository.DEFAULT_SORT);
	}
}
