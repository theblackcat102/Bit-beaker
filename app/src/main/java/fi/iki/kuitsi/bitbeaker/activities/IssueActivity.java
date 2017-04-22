package fi.iki.kuitsi.bitbeaker.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import butterknife.BindView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import fi.iki.kuitsi.bitbeaker.Bitbeaker;
import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.MarkupHelper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.adapters.IssueCommentsAdapter;
import fi.iki.kuitsi.bitbeaker.clicklisteners.UserProfileActivityStartingClickListener;
import fi.iki.kuitsi.bitbeaker.data.api.resource.IssueKindResourceProvider;
import fi.iki.kuitsi.bitbeaker.data.api.resource.IssuePriorityResourceProvider;
import fi.iki.kuitsi.bitbeaker.data.api.resource.IssueStatusResourceProvider;
import fi.iki.kuitsi.bitbeaker.data.api.resource.StringProvider;
import fi.iki.kuitsi.bitbeaker.domainobjects.Issue;
import fi.iki.kuitsi.bitbeaker.domainobjects.IssueComment;
import fi.iki.kuitsi.bitbeaker.domainobjects.Privilege;
import fi.iki.kuitsi.bitbeaker.network.RestService;
import fi.iki.kuitsi.bitbeaker.network.SpiceServiceListenerAdapter;
import fi.iki.kuitsi.bitbeaker.network.request.RequestPrivileges;
import fi.iki.kuitsi.bitbeaker.network.request.RequestSingleIssue;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.IssueCommentsRequest;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.UpdateIssueRequest;
import fi.iki.kuitsi.bitbeaker.network.request.user.RequestUser;
import fi.iki.kuitsi.bitbeaker.network.response.user.UserEndpoint;
import fi.iki.kuitsi.bitbeaker.util.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.keyboardsurfer.android.widget.crouton.Style;

public class IssueActivity extends BaseRepositoryActivity {

	static final int NEW_ISSUE_COMMENT = 0;
	static final int UPDATE_ISSUE = 1;
	private static final String TAG = IssueActivity.class.getSimpleName();

	@BindView(R.id.issue_comments_list) ListView listView;

	private final StringProvider<Issue.Kind> issueKindResourceProvider = new IssueKindResourceProvider();
	private final StringProvider<Issue.Priority> issuePriorityResourceProvider = new IssuePriorityResourceProvider();
	private final StringProvider<Issue.Status> issueStatusResourceProvider = new IssueStatusResourceProvider();
	private final SpiceManager spiceManager = new SpiceManager(RestService.class);
	private final ProgressBarController progressBarController = new ProgressBarController();

	private boolean issueLoaded = false;
	private int id;
	private LinearLayout listViewHeader;
	private IssueCommentsAdapter listAdapter;

	private boolean isResolved = false;

	private boolean displayResolveMenu = false;
	private boolean displayEditMenu = false;

	private String reportedByUsername;

	public IssueActivity() {
		super(R.layout.issue);
	}

	/**
	 * Create an intent for this activity.
	 */
	public static Intent createIntent(Context context, String owner, String slug, int id) {
		Intent intent = new Intent(context, IssueActivity.class);
		addExtendedDataToIntent(intent, owner, slug);
		intent.putExtra("id", id);
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Uri data = getIntent().getData();
		if (data != null && !getIntent().hasExtra("id")) {
			List<String> segments = data.getPathSegments();
			if (segments.size() >= 4) {
				try {
					Intent intent = createIntent(this, segments.get(0), segments.get(1), Integer.parseInt(segments.get(3)));
					finish();
					startActivity(intent);
				} catch (NumberFormatException e) {
					if (segments.get(3).equalsIgnoreCase("new")) {
						Intent intent = NewIssueActivity.createIntent(this, segments.get(0), segments.get(1));
						finish();
						startActivity(intent);
					} else {
						Log.d(TAG, "Issue id could not be parsed from URL");
						finish();
					}
				}
			}
		}

		setTitle(getSlug());
		setToolbarSubtitle(String.format(getString(R.string.issue_id), id));

		listViewHeader = (LinearLayout) getLayoutInflater().inflate(R.layout.listitem_issue_header,
				listView, false);
		listView.addHeaderView(listViewHeader);
		listAdapter = new IssueCommentsAdapter(this, getOwner(), getSlug());
		listView.setAdapter(listAdapter);
	}

	@Override
	protected void getExtras() {
		super.getExtras();
		id = getIntent().getIntExtra("id", -1);
	}

	@Override
	protected void onStart() {
		super.onStart();
		spiceManager.start(this);
		spiceManager.addSpiceServiceListener(progressBarController);
		loadIssue();
	}

	@Override
	public void onResume() {
		super.onResume();
		progressBarController.attachView(toolbarProgressBar);
	}

	@Override
	public void onPause() {
		progressBarController.detachView(toolbarProgressBar);
		super.onPause();
	}

	@Override
	protected void onStop() {
		spiceManager.removeSpiceServiceListener(progressBarController);
		spiceManager.shouldStop();
		super.onStop();
	}

	private void loadIssue() {
		RequestSingleIssue issuesRequest = new RequestSingleIssue(getOwner(), getSlug(), id);
		spiceManager.execute(issuesRequest, new IssueRequestListener());
	}

	private void loadComments() {
		IssueCommentsRequest request = new IssueCommentsRequest(getOwner(), getSlug(), id);
		spiceManager.execute(request, new IssueCommentRequestListener());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_manage_issue, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem editItem = menu.findItem(R.id.menu_update);
		MenuItem resolveItem = menu.findItem(R.id.menu_resolve);
		if (issueLoaded) {
			editItem.setVisible(displayEditMenu);
			resolveItem.setVisible(!isResolved && displayResolveMenu);
		} else {
			editItem.setVisible(false);
			resolveItem.setVisible(false);
		}

		return true;
	}

	protected void maybeInvalidateMenu() {
		supportInvalidateOptionsMenu();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_update: {
				Intent intent = new Intent(this, UpdateIssueActivity.class);
				// Just passing the current bundle instead of creating a new one!
				intent.putExtras(getIntent().getExtras());
				startActivityForResult(intent, UPDATE_ISSUE);
				return true;
			}
			case R.id.menu_add: {
				Intent intent = new Intent(this, NewIssueCommentActivity.class);
				intent.putExtras(getIntent().getExtras()); // Just passing the current bundle instead of creating a new one!
				startActivityForResult(intent, NEW_ISSUE_COMMENT);
				return true;
			}
			case R.id.menu_resolve: {
				resolveIssue();
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == NEW_ISSUE_COMMENT && resultCode == RESULT_OK) {
			Crouton.showText(this, R.string.comment_submitted, Style.INFO);
			loadComments();
		} else if (requestCode == UPDATE_ISSUE && resultCode == RESULT_OK) {
			loadIssue();
		}
	}

	private void resolveIssue() {
		SpiceRequest<Issue> resolveIssueRequest =
				new UpdateIssueRequest.Builder(getOwner(), getSlug(), id)
						.setStatus(Issue.Status.RESOLVED).build();
		spiceManager.execute(resolveIssueRequest, new UpdateIssueRequestListener());
	}

	public void displayIssue(Issue issue) {
		TextView issueTitle = (TextView) findViewById(R.id.issue_title);
		TextView issueStatus = (TextView) findViewById(R.id.issueStatus);
		TextView issueType = (TextView) findViewById(R.id.issueType);
		TextView issuePriority = (TextView) findViewById(R.id.issuePriority);
		TextView issueAuthor = (TextView) findViewById(R.id.issueAuthor);
		TextView issueCreationDate = (TextView) findViewById(R.id.issueTimestamp);
		TextView issueMilestone = (TextView) findViewById(R.id.issueMilestone);
		TextView issueComponent = (TextView) findViewById(R.id.issueComponent);
		TextView issueVersion = (TextView) findViewById(R.id.issueVersion);
		TextView issueResponsible = (TextView) findViewById(R.id.issueResponsible);

		String milestone = "--";
		String version = "--";
		String component = "--";

		issueMilestone.setText(milestone);
		issueComponent.setText(component);
		issueVersion.setText(version);

		String title = issue.getTitle();
		issueTitle.setText(title);

		String issueCreator;
		boolean isAnonymous = false;
		try {
			issueCreator = issue.getReportedBy().getUsername();
		} catch (NullPointerException e) {
			issueCreator = this.getString(R.string.issueReporterAnonymous);
			isAnonymous = true;
		}
		issueStatus.setText(issueStatusResourceProvider.getStringRes(issue.getStatus()));
		if (Issue.Status.RESOLVED.equals(issue.getStatus())) {
			isResolved = true;
		} else {
			isResolved = false;
		}

		String issueAssignee = "--";

		if (issue.getResponsible() != null) {
			issueAssignee = issue.getResponsible().getDisplayName();
		}

		issueResponsible.setText(issueAssignee);

		issueType.setText(issueKindResourceProvider.getStringRes(issue.getKind()));
		issuePriority.setText(issuePriorityResourceProvider.getStringRes(issue.getPriority()));
		issueAuthor.setText(issueCreator);
		if (!isAnonymous) {
			issueAuthor.setOnClickListener(new UserProfileActivityStartingClickListener(issueCreator));
			Helper.renderAsLink(issueAuthor);
		}
		issueCreationDate.setText(Helper.formatDate(issue.getCreatedOn()));

		milestone = issue.getMetadata().getMilestone();
		component = issue.getMetadata().getComponent();
		version = issue.getMetadata().getVersion();

		if (StringUtils.isNotEmpty(milestone)) {
			issueMilestone.setText(milestone);
		}

		if (StringUtils.isNotEmpty(component)) {
			issueComponent.setText(component);
		}

		if (StringUtils.isNotEmpty(version)) {
			issueVersion.setText(version);
		}

		TextView issueContent = (TextView) listViewHeader.findViewById(R.id.issue_content);
		TextView comments_count = (TextView) listViewHeader.findViewById(R.id.issue_comments_count);

		boolean useCreole = MarkupHelper.isCreole(issue.getLastUpdated());
		if (Bitbeaker.REPO_OWNER.equalsIgnoreCase(getOwner())
				&& Bitbeaker.REPO_SLUG.equalsIgnoreCase(getSlug()) && id == 31) {
			// Creole markup testing issue. We can still use raw contents of if even though it
			// is rendered in Markdown on Bitbucket website
			useCreole = true;
		}

		issueContent.setText(MarkupHelper.handleMarkup(issue.getContent(), getOwner(), getSlug(),
				useCreole));
		issueContent.setMovementMethod(LinkMovementMethod.getInstance());
		comments_count.setText(getString(R.string.issue_comments) + " " + issue.getCommentCount());

		if (issue.getReportedBy() != null) {
			reportedByUsername = issue.getReportedBy().getUsername();
		}
	}

	protected void performUserRequest() {
		RequestUser userRequest = new RequestUser();
		spiceManager.execute(userRequest, userRequest.getCacheKey(),
				userRequest.getCacheExpireDuration(), new UserRequestListener());
	}

	protected void performPrivilegeRequest(String user) {
		RequestPrivileges requestPrivileges = new RequestPrivileges(getOwner(), getSlug(), user);
		spiceManager.execute(requestPrivileges, requestPrivileges.getCacheKey(),
				requestPrivileges.getCacheExpireDuration(), new PrivilegeRequestListener());
	}

	final class IssueCommentRequestListener implements RequestListener<IssueComment.List> {
		@Override
		public void onRequestFailure(SpiceException spiceException) {
		}

		@Override
		public void onRequestSuccess(IssueComment.List comments) {
			listAdapter.clear();
			listAdapter.addAll(comments);
		}

	}

	final class UserRequestListener implements RequestListener<UserEndpoint> {

		@Override
		public void onRequestFailure(SpiceException e) {
			Crouton.showText(IssueActivity.this, R.string.milestones_loading_error, Style.ALERT);
		}

		@Override
		public void onRequestSuccess(final UserEndpoint user) {
			if (user != null && (user.getUser().getUsername().equalsIgnoreCase(getOwner())
					|| user.getUser().getUsername().equals(reportedByUsername))) {
				// The currently logged in user is the owner, of course the owner
				// is allowed to create new milestones.
				displayResolveMenu = true;
				displayEditMenu = true;
				maybeInvalidateMenu();
			} else {
				performPrivilegeRequest(user.getUser().getUsername());
			}
		}
	}

	final class PrivilegeRequestListener implements RequestListener<Privilege.List> {
		@Override
		public void onRequestFailure(SpiceException e) {
			displayEditMenu = false;
			displayResolveMenu = false;
			maybeInvalidateMenu();
		}

		@Override
		public void onRequestSuccess(final Privilege.List privilege) {
			// Right now this indicates the current user has administrative rights on
			// the current looked at repository, so the user is allowed to create milestones...
			displayEditMenu = true;
			displayResolveMenu = true;
			maybeInvalidateMenu();
		}
	}

	final class UpdateIssueRequestListener implements RequestListener<Issue> {
		@Override
		public void onRequestFailure(SpiceException e) {
			Crouton.showText(IssueActivity.this, R.string.issue_resolve_error, Style.ALERT);
		}

		@Override
		public void onRequestSuccess(final Issue issue) {
			loadIssue();
		}
	}

	final class IssueRequestListener implements RequestListener<Issue> {
		@Override
		public void onRequestFailure(SpiceException spiceException) {

		}

		@Override
		public void onRequestSuccess(Issue issue) {
			issueLoaded = true;
			displayIssue(issue);
			loadComments();
			performUserRequest();
			supportInvalidateOptionsMenu();
		}
	}

	private static class ProgressBarController extends SpiceServiceListenerAdapter {

		private final AtomicInteger count;
		private View progressBar;

		ProgressBarController() {
			this.count = new AtomicInteger(0);
		}

		@Override
		public void onRequestAdded(CachedSpiceRequest<?> request,
				RequestProcessingContext requestProcessingContext) {
			if (count.incrementAndGet() > 0) {
				if (progressBar != null) {
					progressBar.setVisibility(View.VISIBLE);
				}
			}
		}

		@Override
		public void onRequestProcessed(CachedSpiceRequest<?> cachedSpiceRequest,
				RequestProcessingContext requestProcessingContext) {
			if (count.get() > 0) {
				if (count.decrementAndGet() == 0) {
					if (progressBar != null) {
						progressBar.setVisibility(View.GONE);
					}
				}
			}
		}

		public void attachView(ProgressBar progressBar) {
			if (progressBar == null) {
				throw new IllegalArgumentException("progressBar must be not null");
			}
			this.progressBar = progressBar;
			if (count.get() > 0) {
				this.progressBar.setVisibility(View.VISIBLE);
			} else {
				this.progressBar.setVisibility(View.GONE);
			}
		}

		public void detachView(ProgressBar progressBar) {
			this.progressBar.setVisibility(View.GONE);
			this.progressBar = null;
		}
	}
}
