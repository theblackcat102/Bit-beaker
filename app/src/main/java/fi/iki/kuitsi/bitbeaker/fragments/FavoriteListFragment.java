package fi.iki.kuitsi.bitbeaker.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import fi.iki.kuitsi.bitbeaker.FavoritesService;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.activities.RepositoryActivity;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;
import fi.iki.kuitsi.bitbeaker.provider.BitbeakerContract;
import fi.iki.kuitsi.bitbeaker.provider.FavoritesLoader;
import fi.iki.kuitsi.bitbeaker.provider.FavoritesProvider;

/**
 * A {@link ListFragment} showing a list of favorite repositories.
 */
public class FavoriteListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final int TOKEN = 1;

	private FavoritesService favService;
	private CursorAdapter adapter;

	private final ContentObserver observer = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			onChange(selfChange, null);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			if (isAdded()) {
				getLoaderManager().restartLoader(TOKEN, null, FavoriteListFragment.this);
			}
		}
	};


	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		context.getContentResolver().registerContentObserver(
				BitbeakerContract.Repository.CONTENT_STARRED_URI, true, observer);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new FavoriteRepositoryAdapter(getActivity());
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getString(R.string.no_repositories_found));
		setListAdapter(adapter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		favService = FavoritesProvider.getInstance(getActivity());
		getLoaderManager().initLoader(TOKEN, null, this);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		getActivity().getContentResolver().unregisterContentObserver(observer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		final Cursor cursor = (Cursor) adapter.getItem(position);
		Repository repository = FavoritesProvider.cursorToDomainObject(cursor);
		Intent intent = RepositoryActivity.createIntent(getActivity(),
				repository.getOwner(), repository.getSlug());
		startActivity(intent);
	}

	// LoaderCallbacks interface
	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new FavoritesLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		adapter.changeCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		adapter.changeCursor(null);
	}

	/**
	 * {@link CursorAdapter} that renders favorite repositories.
	 */
	private class FavoriteRepositoryAdapter extends ResourceCursorAdapter {

		public FavoriteRepositoryAdapter(Context context) {
			super(context, R.layout.listitem_two_rows_icon_checkbox, null, 0);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView title = (TextView) view.findViewById(R.id.title);
			TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
			CheckBox favRepo = (CheckBox) view.findViewById(R.id.fav_repo);
			ImageView privateIcon = (ImageView) view.findViewById(R.id.icon);

			final Repository repository = FavoritesProvider.cursorToDomainObject(cursor);

			title.setText(repository.getDisplayName());
			subtitle.setText(repository.getOwner());
			favRepo.setChecked(true);
			favRepo.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					CheckBox checkbox = (CheckBox) v;
					if (checkbox.isChecked()) {
						favService.saveFavoriteRepository(repository);
					} else {
						favService.removeFavoriteRepository(repository);
					}
				}
			});
			if (repository.isPrivateRepository()) {
				privateIcon.setVisibility(View.VISIBLE);
			} else {
				privateIcon.setVisibility(View.INVISIBLE);
			}
		}
	}
}
