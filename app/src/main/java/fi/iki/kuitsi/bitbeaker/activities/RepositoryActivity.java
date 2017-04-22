package fi.iki.kuitsi.bitbeaker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.SpiceServiceListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import de.keyboardsurfer.android.widget.crouton.Style;
import fi.iki.kuitsi.bitbeaker.Bitbeaker;
import fi.iki.kuitsi.bitbeaker.FavoritesService;
import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.data.api.model.repositories.DirectoryContent;
import fi.iki.kuitsi.bitbeaker.data.api.interactor.DirectoryContentRequest;
import fi.iki.kuitsi.bitbeaker.data.remote.BitbucketApiCallCompiler;
import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;
import fi.iki.kuitsi.bitbeaker.fragments.RepositoryFragment;
import fi.iki.kuitsi.bitbeaker.navigationdrawer.Item;
import fi.iki.kuitsi.bitbeaker.navigationdrawer.ListItem;
import fi.iki.kuitsi.bitbeaker.network.RestService;
import fi.iki.kuitsi.bitbeaker.network.SpiceServiceListenerAdapter;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.BranchListRequest;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.MainBranchRequest;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.RawContentRequest;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.RepositoryRequest;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.BranchNames;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.MainBranch;
import fi.iki.kuitsi.bitbeaker.provider.FavoritesProvider;

public class RepositoryActivity extends BaseRepositoryActivity {

	private static final String TAG = RepositoryActivity.class.getSimpleName();

	/**
	 * List of accepted README file names.
	 * https://blog.bitbucket.org/2011/05/13/dress-up-your-repository-with-a-readme/
	 */
	private static final List<String> README_FILES = Arrays.asList(
			"README",
			"README.markdown",
			"README.md",
			"README.mkdn",
			//TODO: handle rst and textile
			//"README.rst",
			//"README.textile",
			"README.txt",
			"READ.ME",
			"readme",
			"readme.markdown",
			"readme.md",
			"readme.mkdn",
			//"readme.rst",
			//"readme.textile",
			"readme.txt",
			"read.me");

	private String slug;
	private String owner;
	protected String[] branches = new String[0];
	protected String defaultBranch = null;
	private Repository repo;
	private final SpiceManager spiceManager = new SpiceManager(RestService.class);
	private final SpiceServiceListener spiceServiceListener = new SpiceServiceListenerAdapter() {
		@Override
		protected void onIdle() {
			showProgressBar(false);
		}
		@Override
		protected void onActive() {
			showProgressBar(true);
		}
	};

	private static final int ID_GROUP_COPY_URL = 1;
	private static final int ID_GROUP_TOGGLE_FAVORITE = 2;
	private static final int ID_ITEM_COPY_URL_SSH = 1;
	private static final int ID_ITEM_COPY_URL_HTTPS = 2;
	private static final int ID_ITEM_COPY_URL_WEB = 3;
	private static final int ID_ITEM_TOGGLE_FAVORITE_ON = 1;
	private static final int ID_ITEM_TOGGLE_FAVORITE_OFF = 2;

	@BindView(R.id.sliding_panel_layout) SlidingPaneLayout slidingPaneLayout;
	@BindView(R.id.sliding_panel_container)  FrameLayout slidingPanelContainer;

	public RepositoryActivity() {
		super(R.layout.activity_slidingpanel_toolbar);
	}

	/**
	 * Create an intent for this activity.
	 */
	public static Intent createIntent(Context context, String owner, String slug) {
		Intent intent = new Intent(context, RepositoryActivity.class);
		return BaseRepositoryActivity.addExtendedDataToIntent(intent, owner, slug);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		slug = getSlug();
		owner = getOwner();

		setTitle(R.string.repository);

		slidingPaneLayout.setSliderFadeColor(ContextCompat.getColor(this, android.R.color.transparent));

		setInitialFragment(R.id.fragment_container, new RepositoryFragment(),
				RepositoryFragment.class.getCanonicalName());

		ScrollView scrollView = new ScrollView(this);
		ScrollView.LayoutParams svlayoutParams = new ScrollView.LayoutParams(
				ScrollView.LayoutParams.MATCH_PARENT,
				ScrollView.LayoutParams.MATCH_PARENT);
		scrollView.setLayoutParams(svlayoutParams);

		LinearLayout linearLayout = new LinearLayout(this);
		LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setLayoutParams(linearLayoutParams);

		Item[] items = new Item[] {
				new ListItem.Builder().setIconResId(R.drawable.ab_icon_source).setStrId(R.string.source).setAction(Item.CLICK_ACTION_SOURCE).build(),
				new ListItem.Builder().setIconResId(R.drawable.ab_icon_commits).setStrId(R.string.changesets).setAction(Item.CLICK_ACTION_COMMITS).build(),
				new ListItem.Builder().setIconResId(R.drawable.ab_icon_pullreq).setStrId(R.string.pull_requests).setAction(Item.CLICK_ACTION_PULL_REQUESTS).build(),
				new ListItem.Builder().setIconResId(R.drawable.ab_icon_issues).setStrId(R.string.issues).setAction(Item.CLICK_ACTION_ISSUES).setInitialState(Item.STATE_LOADING).build(),
				new ListItem.Builder().setIconResId(R.drawable.ab_icon_wiki).setStrId(R.string.wiki).setAction(Item.CLICK_ACTION_WIKI).setInitialState(Item.STATE_LOADING).build(),
				new ListItem.Builder().setIconResId(R.drawable.ab_icon_followers).setStrId(R.string.followers).setAction(Item.CLICK_ACTION_FOLLOWERS).build()
		};

		//noinspection ForLoopReplaceableByForEach
		for (int i = 0; i < items.length; ++i) {
			View view = inflateDrawerItem(items[i], linearLayout);
			linearLayout.addView(view);
		}
		scrollView.addView(linearLayout);
		slidingPanelContainer.addView(scrollView);
	}

	@CheckResult @NonNull
	protected View inflateDrawerItem(final Item item, ViewGroup container) {
		View view = item.getView(getLayoutInflater(), container);

		@Item.ClickAction final int clickAction = item.getAction();

		if (clickAction != Item.CLICK_ACTION_NOTHING) {
			view.setFocusable(true);
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					handleSlidePanelItemClick(clickAction);
				}
			});
		}
		view.setTag(clickAction);

		return view;
	}

	private void setItemState(@Item.ClickAction int clickAction, @ListItem.State int state) {
		View view = slidingPanelContainer.findViewWithTag(clickAction);
		if (view == null) return;
		if (state == Item.STATE_HIDDEN) {
			view.setVisibility(View.GONE);
		} else if (state == Item.STATE_VISIBLE) {
			View progressView = view.findViewById(R.id.progressBar);
			if (progressView != null) {
				progressView.setVisibility(View.GONE);
			}
		}
	}

	private void handleSlidePanelItemClick(@Item.ClickAction int action) {
		switch (action) {
			case Item.CLICK_ACTION_SOURCE:
				if (branches == null || branches.length == 0 || defaultBranch == null) {
					showToast(R.string.please_wait_still_loading);
					break;
				}
				startActivity(SourceBrowserActivity.createIntent(this, owner, slug, branches,
						(!"".equals(defaultBranch) ? defaultBranch : branches[0]), "/"));
				break;
			case Item.CLICK_ACTION_COMMITS:
				startActivity(ChangesetListActivity.createIntent(this, owner, slug));
				break;
			case Item.CLICK_ACTION_PULL_REQUESTS:
				startActivity(PullRequestActivity.createIntent(this, owner, slug));
				break;
			case Item.CLICK_ACTION_ISSUES:
				if (repo == null) {
					showToast(R.string.please_wait_still_loading);
					break;
				}
				if (!repo.hasIssues()) {
					//showToast("Issue tracker is disabled"); // TODO extract string resource
					break;
				}
				startActivity(IssuesActivity.createIntent(this, owner, slug));
				break;
			case Item.CLICK_ACTION_WIKI:
				if (repo == null) {
					showToast(R.string.please_wait_still_loading);
					break;
				}
				if (!repo.hasWiki()) {
					//showToast("Wiki is disabled"); // TODO extract string resource
					break;
				}
				startActivity(WikiActivity.createIntent(this, owner, slug, null));
				break;
			case Item.CLICK_ACTION_FOLLOWERS:
				startActivity(RepositoryFollowersActivity.createIntent(this, owner, slug));
				break;
			case Item.CLICK_ACTION_NOTHING:
			default:
				break;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		spiceManager.start(this);
		getRepository();
		listBranches();
		getDefaultBranch();
	}

	@Override
	public void onResume() {
		super.onResume();
		spiceManager.addSpiceServiceListener(spiceServiceListener);
	}

	@Override
	public void onPause() {
		spiceManager.removeSpiceServiceListener(spiceServiceListener);
		super.onPause();
	}

	@Override
	protected void onStop() {
		spiceManager.shouldStop();
		super.onStop();
	}

	// repository
	private void getRepository() {
		RepositoryRequest request = new RepositoryRequest(owner, slug);
		spiceManager.execute(request, request.getCacheKey(), request.getCacheExpireDuration(),
				new RepositoryRequestListener());
	}

	private class RepositoryRequestListener implements RequestListener<Repository> {
		@Override
		public void onRequestFailure(SpiceException spiceException) {
		}

		@Override
		public void onRequestSuccess(Repository repository) {
			RepositoryFragment fragment = findFragmentById(R.id.fragment_container);
			fragment.setRepository(repository);
			repo = repository;

			setItemState(Item.CLICK_ACTION_WIKI, repo.hasWiki() ? Item.STATE_VISIBLE : Item.STATE_HIDDEN);
			setItemState(Item.CLICK_ACTION_ISSUES, (repo.hasIssues() ? Item.STATE_VISIBLE : Item.STATE_HIDDEN));
			supportInvalidateOptionsMenu();
		}
	}

	// main branch
	private void getDefaultBranch() {
		MainBranchRequest request = new MainBranchRequest(owner, slug);
		spiceManager.execute(request, request.getCacheKey(), request.getCacheExpireDuration(),
				new MainBranchRequestListener());
	}

	private class MainBranchRequestListener implements RequestListener<MainBranch> {
		@Override
		public void onRequestFailure(SpiceException spiceException) {
			showToast(R.string.no_branches, Style.ALERT);
			defaultBranch = "";
		}

		@Override
		public void onRequestSuccess(MainBranch mainBranch) {
			defaultBranch = mainBranch.name;
			getRootDirectoryContent(defaultBranch);
		}
	}

	// branch list
	private void listBranches() {
		BranchListRequest request = new BranchListRequest(owner, slug);
		spiceManager.execute(request, request.getCacheKey(), request.getCacheExpireDuration(),
				new BranchListRequestListener());
	}

	private class BranchListRequestListener implements RequestListener<BranchNames> {
		@Override
		public void onRequestFailure(SpiceException spiceException) {
			showToast(R.string.no_branches, Style.ALERT);
		}

		@Override
		public void onRequestSuccess(BranchNames branchNames) {
			branches = new String[branchNames.size()];
			branchNames.toArray(branches);
			supportInvalidateOptionsMenu();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu copymenu  = menu.addSubMenu(ID_GROUP_COPY_URL, Menu.NONE, Menu.NONE,
				R.string.repository_copy_url);
		copymenu.add(ID_GROUP_COPY_URL, ID_ITEM_COPY_URL_SSH, Menu.NONE, R.string.url_ssh);
		copymenu.add(ID_GROUP_COPY_URL, ID_ITEM_COPY_URL_HTTPS, Menu.NONE, R.string.url_https);
		copymenu.add(ID_GROUP_COPY_URL, ID_ITEM_COPY_URL_WEB, Menu.NONE, R.string.url_web);

		menu.add(ID_GROUP_TOGGLE_FAVORITE, ID_ITEM_TOGGLE_FAVORITE_ON, Menu.NONE,
				R.string.favorites_add);
		menu.add(ID_GROUP_TOGGLE_FAVORITE, ID_ITEM_TOGGLE_FAVORITE_OFF, Menu.NONE,
				R.string.favorites_remove);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		FavoritesService favService = FavoritesProvider.getInstance(this);
		for (int i = 0; i < menu.size(); i++) {
			MenuItem item = menu.getItem(i);
			if (item.getGroupId() == ID_GROUP_TOGGLE_FAVORITE) {
				if (repo == null) {
					item.setVisible(false);
				} else {
					boolean isFavorite = favService.isFavoriteRepository(repo);
					if (item.getItemId() == ID_ITEM_TOGGLE_FAVORITE_ON) {
						item.setVisible(!isFavorite);
					}
					if (item.getItemId() == ID_ITEM_TOGGLE_FAVORITE_OFF) {
						item.setVisible(isFavorite);
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getGroupId() == ID_GROUP_COPY_URL && item.getItemId() != Menu.NONE) {
			if (Helper.copyStringToClipboard(getRepoUrl(item.getItemId()))) {
				showToast(R.string.repository_url_copied);
			}
			return true;
		} else if (item.getGroupId() == ID_GROUP_TOGGLE_FAVORITE) {
			if (repo == null) {
				return false;
			}
			FavoritesService favService = FavoritesProvider.getInstance(this);
			if (item.getItemId() == ID_ITEM_TOGGLE_FAVORITE_ON) {
				favService.saveFavoriteRepository(repo);
			} else /*if (item.getItemId() == ID_ITEM_TOGGLE_FAVORITE_OFF)*/ {
				favService.removeFavoriteRepository(repo);
			}
			supportInvalidateOptionsMenu();
			return true;
		}
		return super.onOptionsItemSelected(item);

	}

	private void getRootDirectoryContent(String defaultBranch) {
		BitbucketApiCallCompiler.using(spiceManager)
				.apiCall(DirectoryContentRequest.create(owner, slug, defaultBranch))
				.listener(new RepoSourceRequestListener())
				.cacheExpiredOn(5, TimeUnit.MINUTES)
				.cacheKeyPrefix("reposource")
				.execute();
	}

	private class RepoSourceRequestListener implements RequestListener<DirectoryContent> {
		@Override
		public void onRequestFailure(SpiceException e) {
			Log.e(TAG, "RepoSourceRequest failed", e);
		}

		@Override
		public void onRequestSuccess(DirectoryContent directoryContent) {
			final List<DirectoryContent.RepoFile> rootFiles = directoryContent.getFiles();
			for (int i = 0; i < README_FILES.size(); ++i) {
				if (rootFiles.contains(new DirectoryContent.RepoFile(README_FILES.get(i)))) {
					Log.d(TAG, "file: " + README_FILES.get(i));
					RawContentRequest request = new RawContentRequest(owner, slug, defaultBranch,
							README_FILES.get(i));
					spiceManager.execute(request, request.getCacheKey(),
							request.getCacheExpireDuration(), new ReadmeRequestListener());
					return;
				}
			}
			Log.d(TAG, "README not found");
		}
	}

	private class ReadmeRequestListener implements RequestListener<String> {
		@Override
		public void onRequestFailure(SpiceException e) {
			Log.e(TAG, "README request failed", e);
		}

		@Override
		public void onRequestSuccess(String fileContent) {
			RepositoryFragment fragment = findFragmentById(R.id.fragment_container);
			if (fragment != null) {
				fragment.setReadme(owner, slug, fileContent);
			}
		}
	}

	/**
	 * Returns repository's url of selected type.
	 *
	 * @param urlType must be {@link #ID_ITEM_COPY_URL_SSH}, {@link #ID_ITEM_COPY_URL_HTTPS}
	 *                or {@link #ID_ITEM_COPY_URL_WEB}
	 * @return Repository URL of selected type
	 */
	private String getRepoUrl(int urlType) {
		String dvcs = (repo != null ? repo.getScm() : ""); // fall back to web url
		String username = Bitbeaker.get(this).getUsername();
		StringBuilder sb = new StringBuilder();

		if ("git".equalsIgnoreCase(dvcs)) {
			/* Git:
			 * https://<user>@bitbucket.org/<owner>/<slug>.git (https with authentication)
			 *        https://bitbucket.org/<owner>/<slug>.git (https without authentication)
			 *            git@bitbucket.org:<owner>/<slug>.git (ssh)
			 *        https://bitbucket.org/<owner>/<slug> (www)
			 */
			if (urlType == ID_ITEM_COPY_URL_SSH) {
				sb.append("git@");
			} else {
				sb.append("https://");
			}
			if (!"".equals(username) && urlType == ID_ITEM_COPY_URL_HTTPS) {
				sb.append(username).append("@");
			}
			sb.append("bitbucket.org");
			sb.append((urlType == ID_ITEM_COPY_URL_SSH) ? ":" : "/");
			sb.append(owner).append("/").append(slug);
			if (urlType != ID_ITEM_COPY_URL_WEB) {
				sb.append(".git");
			}
		} else if ("hg".equalsIgnoreCase(dvcs)) {
			/* Hg:
			 * https://<user>@bitbucket.org/<owner>/<slug> (https with authentication)
			 *        https://bitbucket.org/<owner>/<slug> (https without authentication)
			 *       ssh://hg@bitbucket.org/<owner>/<slug> (ssh)
			 *        https://bitbucket.org/<owner>/<slug> (www)
			 */
			if (urlType == ID_ITEM_COPY_URL_SSH) {
				sb.append("ssh://hg@");
			} else {
				sb.append("https://");
			}
			if (!"".equals(username) && urlType == ID_ITEM_COPY_URL_HTTPS) {
				sb.append(username).append("@");
			}
			sb.append("bitbucket.org/").append(owner).append("/").append(slug);
		} else {
			// unknown type, return web url
			sb.append("https://bitbucket.org/").append(owner).append("/").append(slug);
		}

		return sb.toString();
	}

}
