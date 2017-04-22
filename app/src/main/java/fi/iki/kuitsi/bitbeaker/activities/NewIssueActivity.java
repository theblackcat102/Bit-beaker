package fi.iki.kuitsi.bitbeaker.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.List;

import butterknife.BindView;
import de.keyboardsurfer.android.widget.crouton.Style;
import fi.iki.kuitsi.bitbeaker.Bitbeaker;
import fi.iki.kuitsi.bitbeaker.BuildConfig;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.data.api.resource.IssueKindResourceProvider;
import fi.iki.kuitsi.bitbeaker.data.api.resource.IssuePriorityResourceProvider;
import fi.iki.kuitsi.bitbeaker.data.api.resource.StringProvider;
import fi.iki.kuitsi.bitbeaker.domainobjects.Issue;
import fi.iki.kuitsi.bitbeaker.domainobjects.IssueContainer;
import fi.iki.kuitsi.bitbeaker.network.HttpException;
import fi.iki.kuitsi.bitbeaker.network.RestService;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.IssueContainersRequest;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.NewIssueRequest;
import fi.iki.kuitsi.bitbeaker.ui.common.ApiEnumAdapter;

public class NewIssueActivity extends BaseRepositoryActivity {

	// Issue widgets.
	@BindView(R.id.container) ViewAnimator viewAnimator;
	@BindView(R.id.new_issue_title) EditText titleEditor;
	@BindView(R.id.new_issue_content)  EditText contentEditor;
	@BindView(R.id.new_issue_assignee) EditText assigneeEditor;
	@BindView(R.id.new_issue_type) Spinner typeSpinner;
	@BindView(R.id.new_issue_priority) Spinner prioritySpinner;
	@BindView(R.id.new_issue_component) Spinner componentSpinner;
	@BindView(R.id.new_issue_milestone) Spinner milestonesSpinner;
	@BindView(R.id.new_issue_version) Spinner versionSpinner;
	@BindView(R.id.milestone_label) TextView milestoneText;
	@BindView(R.id.version_label) TextView versionText;
	@BindView(R.id.component_label) TextView componentText;

	// will be hidden when creating new issue
	@BindView(R.id.new_issue_status) Spinner statusSpinner;
	@BindView(R.id.StatusLabel) TextView statusLabel;

	protected final SpiceManager spiceManager = new SpiceManager(RestService.class);

	public NewIssueActivity() {
		super(R.layout.new_issue);
	}

	/**
	 * Create an intent for this activity.
	 */
	public static Intent createIntent(Context context, String owner, String slug) {
		Intent intent = new Intent(context, NewIssueActivity.class);
		BaseRepositoryActivity.addExtendedDataToIntent(intent, owner, slug);
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initActionBar();
		initForm();
	}

	@Override
	protected void onStart() {
		super.onStart();
		spiceManager.start(this);
		performRequest();
	}

	@Override
	protected void onStop() {
		spiceManager.shouldStop();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_simple_submit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_submit:
				onSubmit();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	protected void initActionBar() {
		setTitle(R.string.report_new_issue);
		setToolbarSubtitle(getOwner() + "/" + getSlug());
	}

	protected void initForm() {
		initSpinner(typeSpinner, Issue.Kind.class, new IssueKindResourceProvider(), Issue.Kind.BUG);
		initSpinner(prioritySpinner, Issue.Priority.class, new IssuePriorityResourceProvider(), Issue.Priority.MAJOR);
		statusSpinner.setVisibility(View.GONE);
		statusLabel.setVisibility(View.GONE);
		viewAnimator.setDisplayedChild(1);

		if (Bitbeaker.REPO_OWNER.equalsIgnoreCase(getOwner())
				&& Bitbeaker.REPO_SLUG.equalsIgnoreCase(getSlug())
				&& BuildConfig.DEBUG) {
			contentEditor.setText("Exact version " + BuildConfig.VERSION_NAME
					+ "\n-------------------------\n");
		}
	}

	protected <T extends Enum<T>> void initSpinner(Spinner spinner, Class<T> enumType, StringProvider<T> stringProvider, @Nullable Enum<T> selected) {
		ApiEnumAdapter<T> apiEnumAdapter = new ApiEnumAdapter<>(this, enumType, stringProvider);
		spinner.setAdapter(apiEnumAdapter);
		if (selected != null) {
			spinner.setSelection(selected.ordinal());
		}
	}

	protected void initSpinner(final Spinner spinner, final String[] options, int initialIndex) {
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_item, options);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerAdapter);
		spinner.setSelection(initialIndex);
	}

	protected void performRequest() {
		showProgressBar(true);
		requestMilestonesVersionsComponents();
	}

	protected void requestMilestonesVersionsComponents() {
		IssueContainersRequest milestonesRequest = IssueContainersRequest.milestones(getOwner(), getSlug());
		spiceManager.execute(milestonesRequest, new MilestonesRequestListener());
		IssueContainersRequest versionsRequest = IssueContainersRequest.versions(getOwner(), getSlug());
		spiceManager.execute(versionsRequest, new VersionRequestListener());
		IssueContainersRequest componentsRequest = IssueContainersRequest.components(getOwner(), getSlug());
		spiceManager.execute(componentsRequest, new ComponentsRequestListener());
	}

	protected void populateComponentSpinner(IssueContainer.List components) {
		populateSpinner(components, componentSpinner, componentText, R.string.component_choose);
	}

	protected void populateVersionSpinner(IssueContainer.List versions) {
		populateSpinner(versions, versionSpinner, versionText, R.string.version_choose);

		if (Bitbeaker.REPO_OWNER.equalsIgnoreCase(getOwner())
				&& Bitbeaker.REPO_SLUG.equalsIgnoreCase(getSlug())) {
			// Select default version. These are specific to Bitbeaker's issue tracker settings
			if (BuildConfig.DEBUG) {
				for (int i = 0; i < versionSpinner.getAdapter().getCount(); i++) {
					if ("debug".equals(versionSpinner.getAdapter().getItem(i))) {
						versionSpinner.setSelection(i);
						break;
					}
				}
			} else {
				for (int i = 0; i < versionSpinner.getAdapter().getCount(); i++) {
					if (BuildConfig.VERSION_NAME.equals(versionSpinner.getAdapter().getItem(i))) {
						versionSpinner.setSelection(i);
						break;
					}
				}
			}
		}
	}

	protected void populateMilestoneSpinner(IssueContainer.List milestones) {
		populateSpinner(milestones, milestonesSpinner, milestoneText, R.string.milestone_choose);
	}

	protected void showError(SpiceException spiceException) {
		Throwable cause = spiceException.getCause();
		if (cause instanceof HttpException) {
			// Invalid username in Assignee results: spiceException ... Caused by: HttpException: 400 BAD REQUEST
			HttpException error = (HttpException) cause;
			showToast("" + error.code() + " " + error.message());
		}
	}

	private void populateSpinner(final List<? extends IssueContainer> list, final Spinner spinner,
			final TextView textView, @StringRes final int optionZeroKey) {
		String[] options = new String[list.size() + 1];

		options[0] = getString(optionZeroKey);
		options[0] = getString(optionZeroKey);
		int i = 1;
		for (IssueContainer item : list) {
			options[i] = item.getName();
			i++;
		}

		initSpinner(spinner, options, 0);

		if (!list.isEmpty()) {
			textView.setVisibility(View.VISIBLE);
			spinner.setVisibility(View.VISIBLE);
		}
	}

	protected void onSubmit() {
		try {
			String title = titleEditor.getText().toString();
			String content = contentEditor.getText().toString();
			String assignee = assigneeEditor.getText().toString().trim();

			Issue.Kind kind = (Issue.Kind) typeSpinner.getSelectedItem();
			Issue.Priority priority = (Issue.Priority) prioritySpinner.getSelectedItem();

			showProgressBar(true);
			NewIssueRequest.Builder requestBuilder = new NewIssueRequest.Builder(getOwner(), getSlug())
					.setTitle(title)
					.setContent(content)
					.setKind(kind)
					.setPriority(priority)
					.setStatus(Issue.Status.NEW);
			if (milestonesSpinner.getSelectedItemPosition() != 0) {
				requestBuilder.setMilestone(milestonesSpinner.getSelectedItem().toString());
			}
			if (componentSpinner.getSelectedItemPosition() != 0) {
				requestBuilder.setComponent(componentSpinner.getSelectedItem().toString());
			}
			if (versionSpinner.getSelectedItemPosition() != 0) {
				requestBuilder.setVersion(versionSpinner.getSelectedItem().toString());
			}
			if (assignee.length() > 0) {
				requestBuilder.setAssignee(assignee);
			}
			NewIssueRequest request = requestBuilder.build();
			spiceManager.execute(request, new RequestListener<Issue>() {
				@Override
				public void onRequestFailure(SpiceException spiceException) {
					showProgressBar(false);
					showError(spiceException);
				}

				@Override
				public void onRequestSuccess(Issue issue) {
					showProgressBar(false);
					onSubmitted();
				}
			});
		} catch (NullPointerException npe) {
			showToast(R.string.please_wait_still_loading, Style.INFO);
		}
	}

	private void onSubmitted() {
		setResult(Activity.RESULT_OK, getIntent());
		finish();
	}

	final class MilestonesRequestListener implements RequestListener<IssueContainer.List> {
		@Override
		public void onRequestFailure(SpiceException e) {
			showProgressBar(false);
			showToast(R.string.milestones_loading_error, Style.ALERT);
		}

		@Override
		public void onRequestSuccess(final IssueContainer.List milestones) {
			showProgressBar(false);
			populateMilestoneSpinner(milestones);
		}
	}

	final class ComponentsRequestListener implements RequestListener<IssueContainer.List> {
		@Override
		public void onRequestFailure(SpiceException e) {
			showProgressBar(false);
			showToast(R.string.milestones_loading_error, Style.ALERT);
		}

		@Override
		public void onRequestSuccess(final IssueContainer.List components) {
			showProgressBar(false);
			populateComponentSpinner(components);
		}
	}

	final class VersionRequestListener implements RequestListener<IssueContainer.List> {
		@Override
		public void onRequestFailure(SpiceException e) {
			showProgressBar(false);
			showToast(R.string.milestones_loading_error, Style.ALERT);
		}

		@Override
		public void onRequestSuccess(final IssueContainer.List versions) {
			showProgressBar(false);
			populateVersionSpinner(versions);
		}
	}
}
