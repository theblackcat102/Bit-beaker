package fi.iki.kuitsi.bitbeaker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.data.api.model.repositories.DirectoryContent;
import fi.iki.kuitsi.bitbeaker.data.api.interactor.DirectoryContentRequest;
import fi.iki.kuitsi.bitbeaker.data.remote.BitbucketApiCallCompiler;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.TagsRequest;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.Tags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Style;

public class SourceBrowserActivity extends MyActivity implements AdapterView.OnItemClickListener {

	private static final String STATE_SELECTED_REVISION = "selected_revision";
	private static final int ID_GROUP_BRANCHES = 1;// some unique non-zero id for submenu
	private static final int ID_GROUP_TAGS = 2;

	private SubMenu branchesMenu;
	private SubMenu tagsMenu;
	private String slug;
	private String owner;
	private String subdir;
	private String selectedRevision;
	private String[] branches;
	private Tags tags;
	private SourceTreeAdapter adapter;
	private View progressContainer;
	private View listContainer;

	/**
	 * @param revision can be commit hash, branch or tag
	 * @param subdir Directory, use "/" for repository root
	 */
	public static Intent createIntent(Context context, String owner, String slug, String[] branches,
			String revision, String subdir) {
		Intent intent = new Intent(context, SourceBrowserActivity.class);
		intent.putExtra("owner", owner);
		intent.putExtra("slug", slug);
		intent.putExtra("branches", branches);
		intent.putExtra("revision", revision);
		intent.putExtra("subdir", subdir);
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_content);

		progressContainer = findViewById(R.id.progress_container);
		listContainer = findViewById(R.id.list_container);

		ListView listView = ButterKnife.findById(listContainer, android.R.id.list);
		listView.setOnItemClickListener(this);
		adapter = new SourceTreeAdapter(this);
		listView.setAdapter(adapter);

		Bundle b = getIntent().getExtras();
		slug = b.getString("slug");
		owner = b.getString("owner");

		subdir = b.getString("subdir");
		if (!subdir.startsWith("/")) {
			subdir = "/" + subdir;
		}

		branches = b.getStringArray("branches");
		String revision = b.getString("revision");

		if (savedInstanceState != null) {
			selectedRevision = savedInstanceState.getString(STATE_SELECTED_REVISION);
		} else {
			selectedRevision = revision;
		}

		setTitle(slug + " (" + selectedRevision + ")");
		getSupportActionBar().setSubtitle(subdir);

		getData();

		if (tags == null) {
			TagsRequest tagsRequest = new TagsRequest(owner, slug);
			spiceManager.execute(tagsRequest, tagsRequest.getCacheKey(),
					tagsRequest.getCacheExpireDuration(), new TagsRequestListener());
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(STATE_SELECTED_REVISION, selectedRevision);
		super.onSaveInstanceState(outState);
	}

	private void getData() {
		BitbucketApiCallCompiler.using(spiceManager)
				.apiCall(DirectoryContentRequest.create(owner, slug, selectedRevision, subdir))
				.listener(new RepoSourceRequestListener())
				.cacheExpiredOn(5, TimeUnit.MINUTES)
				.cacheKeyPrefix("reposource")
				.execute();
		showProgress();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		branchesMenu = menu.addSubMenu(R.string.branch);
		setBranchesMenuItems();
		// Allow check mark for one item in sub-menu
		branchesMenu.setGroupCheckable(ID_GROUP_BRANCHES, true, true);

		MenuItem branchesMenuItem = branchesMenu.getItem();
		branchesMenuItem.setIcon(R.drawable.ab_icon_branch);
		MenuItemCompat.setShowAsAction(branchesMenuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS
				| MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT);

		tagsMenu = menu.addSubMenu(R.string.tag);
		MenuItem tagsMenuItem = tagsMenu.getItem();
		tagsMenuItem.setIcon(R.drawable.ab_icon_tag);
		MenuItemCompat.setShowAsAction(tagsMenuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS
				| MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT);
		tagsMenuItem.setEnabled(false);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (tags != null) {
			tagsMenu.clear();
			for (String tag : tags.navigableKeySet()) {
				MenuItem item = tagsMenu.add(ID_GROUP_TAGS, Menu.NONE, Menu.NONE, tag);
				item.setChecked(selectedRevision.equals(tag));
			}
			tagsMenu.getItem().setEnabled(true);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	private void setBranchesMenuItems() {
		branchesMenu.clear();
		for (String branch : branches) {
			MenuItem item = branchesMenu.add(ID_GROUP_BRANCHES, Menu.NONE, Menu.NONE, branch);
			item.setChecked(selectedRevision.equals(branch));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getGroupId() == ID_GROUP_BRANCHES
				|| item.getGroupId() == ID_GROUP_TAGS) {
			item.setChecked(true);
			selectedRevision = item.getTitle().toString();
			setTitle(slug + " (" + selectedRevision + ")");
			getData();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (adapter.isDirectory(position)) {
			startActivity(SourceBrowserActivity.createIntent(this, owner, slug, branches,
					selectedRevision, subdir + adapter.getDirectory(position) + "/"));
		} else if (adapter.isFile(position)) {
			startActivity(SourceActivity.createIntent(this, owner, slug, selectedRevision,
					adapter.getFile(position).getPath()));
		}
	}

	private void showList() {
		progressContainer.setVisibility(View.GONE);
		listContainer.setVisibility(View.VISIBLE);
	}

	private void showProgress() {
		progressContainer.setVisibility(View.VISIBLE);
		listContainer.setVisibility(View.GONE);
	}

	private static class SourceTreeAdapter extends BaseAdapter {

		private final LayoutInflater inflater;
		private final Object lock = new Object();
		private List<String> directories = Collections.emptyList();
		private List<DirectoryContent.RepoFile> files = Collections.emptyList();

		public SourceTreeAdapter(Context context) {
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return getDirectoriesCount() + getFilesCount();
		}

		@Override
		public Object getItem(int position) {
			if (isDirectory(position)) {
				return getDirectory(position);
			} else if (isFile(position)) {
				return getFile(position);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = inflater.inflate(R.layout.listitem_one_row_left_icon, parent, false);
			TextView title = (TextView) convertView.findViewById(R.id.title);
			ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
			if (isDirectory(position)) {
				title.setText(getDirectory(position));
				icon.setImageResource(R.drawable.icon_folder);
			} else if (isFile(position)) {
				final String filePath = getFile(position).getPath();
				int lastSlashIndex = filePath.lastIndexOf("/");
				title.setText(filePath.substring(lastSlashIndex + 1));
				icon.setImageResource(R.drawable.icon_file);
			}
			return convertView;
		}

		public int getDirectoriesCount() {
			return directories.size();
		}

		public boolean isDirectory(int position) {
			return position < getDirectoriesCount();
		}

		public String getDirectory(int position) {
			if (isDirectory(position)) {
				return directories.get(position);
			}
			return null;
		}

		public int getFilesCount() {
			return files.size();
		}

		public boolean isFile(int position) {
			return position >= getDirectoriesCount()
					&& position < getDirectoriesCount() + getFilesCount();
		}

		public DirectoryContent.RepoFile getFile(int position) {
			if (isFile(position)) {
				return files.get(position - getDirectoriesCount());
			}
			return null;
		}

		public void setData(List<String> directories, List<DirectoryContent.RepoFile> files) {
			synchronized (lock) {
				if (directories.isEmpty()) {
					this.directories = Collections.emptyList();
				} else {
					this.directories = new ArrayList<>(directories);
					Collections.sort(this.directories);
				}
				if (files.isEmpty()) {
					this.files = Collections.emptyList();
				} else {
					this.files = new ArrayList<>(files);
					Collections.sort(this.files);
				}
			}
			notifyDataSetChanged();
		}
	}

	private class RepoSourceRequestListener implements RequestListener<DirectoryContent> {
		@Override
		public void onRequestFailure(SpiceException spiceException) {
			showList();
			String errorMsg = String.format(getString(R.string.file_not_found), subdir,
					selectedRevision);
			makeCrouton(errorMsg, Style.ALERT);
		}

		@Override
		public void onRequestSuccess(DirectoryContent directoryContent) {
			showList();
			adapter.setData(directoryContent.getDirectories(), directoryContent.getFiles());
		}
	}

	private final class TagsRequestListener implements RequestListener<Tags> {
		public void onRequestFailure(SpiceException e) {
		}

		@Override
		public void onRequestSuccess(final Tags tagList) {
			tags = tagList;
			supportInvalidateOptionsMenu();
		}
	}
}
