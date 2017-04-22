package fi.iki.kuitsi.bitbeaker.preferences;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fi.iki.kuitsi.bitbeaker.FavoritesService;
import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;
import fi.iki.kuitsi.bitbeaker.provider.FavoritesProvider;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public final class FavoritesManager {

	public static final String TAG = "FavoritesManager";
	private static final int REQUEST_PERMISSION = 1;
	private static final File dlDir = Environment.getExternalStoragePublicDirectory(
			Environment.DIRECTORY_DOWNLOADS);
	private static final File jsonFile = new File(dlDir, "bitbeaker-favorites.json");

	private PreferenceFragmentCompat fragment;
	private DialogPreference resetPreference;
	private DialogPreference importPreference;
	private DialogPreference exportPreference;
	private FavoritesService favoritesService;

	public void attach(PreferenceFragmentCompat fragment) {
		this.fragment = fragment;
		resetPreference = (DialogPreference) fragment.findPreference("favorites_reset");
		importPreference = (DialogPreference) fragment.findPreference("favorites_import");
		exportPreference = (DialogPreference) fragment.findPreference("favorites_export");
		init();
	}

	public void detach() {
		resetPreference.setOnCloseListener(null);
		importPreference.setOnCloseListener(null);
		exportPreference.setOnCloseListener(null);
		fragment = null;
	}

	public void onDisplayPreferenceDialog(final DialogPreference dialogPreference) {
		new AlertDialog.Builder(fragment.getContext())
				.setTitle(dialogPreference.getDialogTitle())
				.setMessage(dialogPreference.getDialogMessage())
				.setPositiveButton(dialogPreference.getPositiveButtonText(), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						DialogPreference.OnCloseListener listener = dialogPreference.getOnCloseListener();
						if (listener != null) {
							listener.onDialogClosed();
						}
					}
				})
				.setNegativeButton(dialogPreference.getNegativeButtonText(), null)
				.show();
	}

	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == REQUEST_PERMISSION && permissions.length > 0 && grantResults.length > 0
				&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			Log.d(TAG, permissions[0] + " granted");
			if (READ_EXTERNAL_STORAGE.equals(permissions[0])) {
				doImport();
			} else if (WRITE_EXTERNAL_STORAGE.equals(permissions[0])) {
				doExport();
			}
		}
	}

	private void init() {
		favoritesService = FavoritesProvider.getInstance(fragment.getContext());
		resetPreference.setOnCloseListener(new DialogPreference.OnCloseListener() {
			@Override
			public void onDialogClosed() {
				doReset();
			}
		});
		importPreference.setDialogMessage(
				String.format(fragment.getString(R.string.prefs_favorites_import_summary), jsonFile));
		importPreference.setOnCloseListener(new DialogPreference.OnCloseListener() {
			@Override
			public void onDialogClosed() {
				prepareForImport();
			}
		});
		exportPreference.setDialogMessage(
				String.format(fragment.getString(R.string.prefs_favorites_export_summary), jsonFile));
		exportPreference.setOnCloseListener(new DialogPreference.OnCloseListener() {
			@Override
			public void onDialogClosed() {
				prepareForExport();
			}
		});
	}

	private boolean checkPermission(@NonNull String permission) {
		if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(fragment.getContext(), permission)) {
			if (fragment.shouldShowRequestPermissionRationale(permission)) {
				showExplainPermissionDialog(permission);
			} else {
				requestPermission(permission);
			}
			return false;
		}
		return true;
	}

	private void showExplainPermissionDialog(final @NonNull String permission) {
		new AlertDialog.Builder(fragment.getContext())
				.setMessage(R.string.prefs_favorites_request_permission_message)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.d(TAG, "dialog dismissed");
						requestPermission(permission);
					}
				})
				.show();
	}

	private void requestPermission(@NonNull String permission) {
		Log.d(TAG, "request " + permission);
		fragment.requestPermissions(new String[]{permission}, REQUEST_PERMISSION);
	}

	private void prepareForImport() {
		if (checkPermission(READ_EXTERNAL_STORAGE)) {
			doImport();
		}
	}

	private void prepareForExport() {
		if (checkPermission(WRITE_EXTERNAL_STORAGE)) {
			doExport();
		}
	}

	private void doReset() {
		favoritesService.deleteFavorites();
	}

	private void doImport() {
		if (!Helper.isExternalStorageReadable()) {
			Log.e(TAG, "SD card not readable!");
			return;
		}

		String text;
		try {
			text = FileUtils.readFileToString(jsonFile);
		} catch (IOException e) {
			Log.e(TAG, "Error reading " + jsonFile);
			return;
		}

		List<Repository> repositories = new ArrayList<>();
		JSONArray repositoriesJson;
		try {
			repositoriesJson = new JSONArray(text);
		} catch (JSONException jsone) {
			Log.e(TAG, "Parse error in favorites JSON file!");
			return;
		}

		for (int i = 0; i < repositoriesJson.length(); i++) {
			try {
				JSONObject repoJson = repositoriesJson.getJSONObject(i);
				Repository repo = new Repository(repoJson.getString("owner"), repoJson.getString("slug"))
						.name(repoJson.getString("name"))
						.privateRepository(repoJson.getBoolean("isPrivate"));
				repositories.add(repo);
			} catch (JSONException jsone) {
				Log.e(TAG, "Failed to import a repo at index " + i);
			}
		}

		//TODO: use asynctask!
		favoritesService.addFavorites(repositories);
	}

	private void doExport() {
		if (!Helper.isExternalStorageWritable()) {
			Log.e(TAG, "SD card not mounted!");
			Toast.makeText(fragment.getContext(),
					fragment.getString(R.string.prefs_favorites_export_failed),
					Toast.LENGTH_SHORT).show();
			return;
		}

		List<Repository> favorites = favoritesService.getFavorites();

		JSONArray repositories = new JSONArray();

		for (Repository repo : favorites) {
			JSONObject object = new JSONObject();
			try {
				object.put("owner", repo.getOwner());
				object.put("slug", repo.getSlug());
				object.put("name", repo.getName());
				object.put("isPrivate", repo.isPrivateRepository());
			} catch (JSONException e) {
				Log.e(TAG, "Failed to export " + repo, e);
				continue;
			}
			repositories.put(object);
		}

		try {
			if (!dlDir.isDirectory()) dlDir.mkdirs();
			FileUtils.writeStringToFile(jsonFile, repositories.toString(2));
			Log.d(TAG, "Export done");
		} catch (Exception e) {
			Log.e(TAG, "Failed to write favorites into a file", e);
			Toast.makeText(fragment.getContext(),
					fragment.getString(R.string.prefs_favorites_export_failed),
					Toast.LENGTH_SHORT).show();
		}
	}
}
