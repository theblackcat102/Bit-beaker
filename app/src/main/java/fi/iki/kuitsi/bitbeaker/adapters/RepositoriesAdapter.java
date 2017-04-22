package fi.iki.kuitsi.bitbeaker.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.net.HttpURLConnection;
import java.util.Comparator;

import fi.iki.kuitsi.bitbeaker.AppComponent;
import fi.iki.kuitsi.bitbeaker.AppComponentService;
import fi.iki.kuitsi.bitbeaker.Bitbeaker;
import fi.iki.kuitsi.bitbeaker.FavoritesService;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.activities.RepositoryActivity;
import fi.iki.kuitsi.bitbeaker.data.api.BitbucketService;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;
import fi.iki.kuitsi.bitbeaker.util.ComparisonChain;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RepositoriesAdapter extends ParameterizedAdapter<Repository> {

	protected final FavoritesService favService;
	private final BitbucketService bitbucketService;

	public RepositoriesAdapter(Context context) {
		super(context);
		AppComponent appComponent = AppComponentService.obtain(context.getApplicationContext());
		favService = appComponent.favoriteService();
		bitbucketService = appComponent.bitbucketService();
	}

	protected OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Repository rec = getItem(v.getId());
			Intent intent = RepositoryActivity.createIntent(v.getContext(),
					rec.getOwner(), rec.getSlug());
			v.getContext().startActivity(intent);
		}
	};

	protected OnLongClickListener longClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(final View view) {
			final Repository repository = (Repository) view.getTag();
			final String owner = repository.getOwner();
			if (currentUserIsTheOwner(owner)) {
				return true;
			}
			final String localizedMessage = context.getString(R.string.dialog_remove_own_admin_rights_message);
			final String formattedMessage = String.format(localizedMessage, owner + "/" + repository.getDisplayName());

			new MaterialDialog.Builder(context)
					.title(R.string.dialog_remove_own_admin_rights_title)
					.content(formattedMessage)
					.positiveText(R.string.dialog_yes)
					.negativeText(R.string.dialog_no)
					.onPositive(new MaterialDialog.SingleButtonCallback() {
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
							deleteAccountPrivilegesFromRepository(repository);
						}})
					.show();

			return true;
		}

		private boolean currentUserIsTheOwner(final String owner) {
			return owner != null && owner.equals(Bitbeaker.get(context).getUsername());
		}
	};

	void deleteAccountPrivilegesFromRepository(Repository repository) {
		String username = Bitbeaker.get(context).getUsername();
		if (!username.isEmpty()) {
			bitbucketService.deleteAccountPrivilegesFromRepository(repository.getOwner(), repository.getSlug(), username).enqueue(
					new Callback<Void>() {
						@Override
						public void onResponse(Call<Void> call, Response<Void> response) {
							if (response.isSuccessful()) {
								Toast.makeText(context, R.string.remove_own_admin_rights_successful, Toast.LENGTH_LONG).show();
							} else if (response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
								Toast.makeText(context, R.string.remove_own_admin_rights_forbidden, Toast.LENGTH_LONG).show();
							} else {
								String formattedError = String.format(context.getString(R.string.remove_own_admin_rights_error), response.code());
								Toast.makeText(context, formattedError, Toast.LENGTH_LONG).show();
							}
						}

						@Override
						public void onFailure(Call<Void> call, Throwable t) {
							Log.e("deleteAccountPrivileges", "onFailure", t);
						}
					});
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = inflateViewIfRequired(convertView, R.layout.listitem_two_rows_icon_checkbox);
		view.setId(position);
		TextView title = (TextView) view.findViewById(R.id.title);
		TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
		final Repository item = getItem(position);
		title.setText(item.getDisplayName());
		subtitle.setText(createSubtitle(item));
		if (item.isPrivateRepository()) {
			view.findViewById(R.id.icon).setVisibility(View.VISIBLE);
		} else {
			view.findViewById(R.id.icon).setVisibility(View.INVISIBLE);
		}
		view.setTag(item);
		view.setOnClickListener(clickListener);
		view.setOnLongClickListener(longClickListener);
		view.setLongClickable(true);
		initFavoriteCheckbox(view, item);

		return view;
	}

	private String createSubtitle(final Repository item) {
		StringBuilder subtitle = new StringBuilder(item.getOwner());
		if (item.getLastUpdated() != null) {
			subtitle.append(" | ").append(context.getString(R.string.last_updated)).append(" ")
					.append(DateFormat.format("yyyy-MM-dd", item.getLastUpdated()));
		}
		return subtitle.toString();
	}

	/**
	 * Initializes the checkbox with which one can set a repository
	 * as a favorite one.
	 *
	 * @param view The view that contains the checkbox
	 * @param repo The repository
	 */
	protected void initFavoriteCheckbox(View view, final Repository repo) {
		CheckBox favRepo = (CheckBox) view.findViewById(R.id.fav_repo);
		favRepo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CheckBox checkbox = (CheckBox) v;
				if (checkbox.isChecked()) {
					favService.saveFavoriteRepository(repo);
				} else {
					favService.removeFavoriteRepository(repo);
				}
			}
		});
		//Set the state of the CheckBox:
		favRepo.setChecked(favService.isFavoriteRepository(repo));
	}

	/**
	 * Sorting order for adapter contents.
	 */
	public enum Sort implements Comparator<Repository> {
		OWNER_ASC (R.string.repositories_sorting_order_owner_asc) {
			@Override
			public int compare(Repository lhs, Repository rhs) {
				return ComparisonChain.start()
						.compare(lhs.getOwner(), rhs.getOwner(),
								String.CASE_INSENSITIVE_ORDER)
						.compare(lhs.getDisplayName(), rhs.getDisplayName(),
								String.CASE_INSENSITIVE_ORDER)
						.result();
			}
		},
		REPO_NAME_ASC (R.string.repositories_sorting_order_reponame_asc) {
			@Override
			public int compare(Repository lhs, Repository rhs) {
				return ComparisonChain.start()
						.compare(lhs.getDisplayName(), rhs.getDisplayName(),
								String.CASE_INSENSITIVE_ORDER)
						.compare(lhs.getOwner(), rhs.getOwner(),
								String.CASE_INSENSITIVE_ORDER)
						.result();
			}
		},
		UPDATED_DESC (R.string.repositories_sorting_order_updated_desc) {
			@Override
			public int compare(Repository lhs, Repository rhs) {
				// descending order
				return rhs.getLastUpdated().compareTo(lhs.getLastUpdated());
			}
		};

		@StringRes private final int id;
		Sort(@StringRes int resId) {
			this.id = resId;
		}

		public String toString(Resources r) {
			return r.getString(id);
		}
	}
}
