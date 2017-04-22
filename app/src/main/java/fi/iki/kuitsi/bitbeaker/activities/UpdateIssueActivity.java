package fi.iki.kuitsi.bitbeaker.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import de.keyboardsurfer.android.widget.crouton.Style;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.data.api.resource.IssueKindResourceProvider;
import fi.iki.kuitsi.bitbeaker.data.api.resource.IssuePriorityResourceProvider;
import fi.iki.kuitsi.bitbeaker.data.api.resource.IssueStatusResourceProvider;
import fi.iki.kuitsi.bitbeaker.domainobjects.Issue;
import fi.iki.kuitsi.bitbeaker.domainobjects.IssueContainer;
import fi.iki.kuitsi.bitbeaker.domainobjects.User;
import fi.iki.kuitsi.bitbeaker.network.request.RequestSingleIssue;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.UpdateIssueRequest;

public final class UpdateIssueActivity extends NewIssueActivity {

	private int issueId;

	private String initialTitle = "", initialContent = "", initialAssignee = "",
			initialMilestone = "", initialComponent = "", initialVersion = "";
	private Issue.Kind initialKind;
	private Issue.Priority initialPriority;
	private Issue.Status initialStatus;

	@Override
	protected void getExtras() {
		super.getExtras();
		Bundle b = getIntent().getExtras();
		issueId = b.getInt("id");
	}

	@Nullable
	@Override
	public Intent getSupportParentActivityIntent() {
		Intent intent = super.getSupportParentActivityIntent();
		if (intent != null) {
			intent.putExtra("id", issueId);
		}
		return intent;
	}

	@Override
	protected void initActionBar() {
		setTitle(R.string.update_issue);
		setToolbarSubtitle(String.format(getString(R.string.issue_id), issueId));
	}

	@Override
	protected void initForm() {
		viewAnimator.setDisplayedChild(0);
	}

	@Override
	protected void performRequest() {
		showProgressBar(true);
		RequestSingleIssue issueRequest = new RequestSingleIssue(getOwner(), getSlug(), issueId);
		spiceManager.execute(issueRequest, new IssueRequestListener());
	}

	@Override
	public void onSubmit() {
		try {
			UpdateIssueRequest.Builder requestBuilder = new UpdateIssueRequest.Builder(getOwner(),
					getSlug(), issueId);

			// Only add parameters that have changed.
			if (!initialTitle.equals(titleEditor.getText().toString())) {
				requestBuilder.setTitle(titleEditor.getText().toString());
			}

			if (!initialContent.equals(contentEditor.getText().toString())) {
				requestBuilder.setContent(contentEditor.getText().toString());
			}

			if (!initialAssignee.equals(assigneeEditor.getText().toString())) {
				requestBuilder.setAssignee(assigneeEditor.getText().toString());
			}

			final Issue.Kind newKind = (Issue.Kind) typeSpinner.getSelectedItem();
			if (newKind != null && initialKind != newKind) {
				requestBuilder.setKind(newKind);
			}

			final Issue.Priority newPriority = (Issue.Priority) prioritySpinner.getSelectedItem();
			if (newKind != null && initialPriority != newPriority) {
				requestBuilder.setPriority(newPriority);
			}

			final Issue.Status newStatus = (Issue.Status) statusSpinner.getSelectedItem();
			if (newStatus != null && initialStatus != newStatus) {
				requestBuilder.setStatus(newStatus);
			}

			if (milestonesSpinner.getSelectedItemPosition() != 0) {
				String newMilestone = milestonesSpinner.getSelectedItem().toString();
				if (!initialMilestone.equalsIgnoreCase(newMilestone)) {
					requestBuilder.setMilestone(newMilestone);
				}
			}

			if (componentSpinner.getSelectedItemPosition() != 0) {
				String newComponent = componentSpinner.getSelectedItem().toString();
				if (!initialComponent.equalsIgnoreCase(newComponent)) {
					requestBuilder.setComponent(newComponent);
				}
			}

			if (versionSpinner.getSelectedItemPosition() != 0) {
				String newVersion = versionSpinner.getSelectedItem().toString();
				if (!initialVersion.equalsIgnoreCase(newVersion)) {
					requestBuilder.setVersion(newVersion);
				}
			}

			showProgressBar(true);
			UpdateIssueRequest request = requestBuilder.build();
			spiceManager.execute(request, new RequestListener<Issue>() {
				@Override
				public void onRequestFailure(SpiceException spiceException) {
					showProgressBar(false);
					showError(spiceException);
				}

				@Override
				public void onRequestSuccess(Issue issue) {
					showProgressBar(false);
					setResult(Activity.RESULT_OK, getIntent());
					finish();
				}
			});
		} catch (NullPointerException npe) {
			showToast(R.string.please_wait_still_loading, Style.INFO);
		}
	}

	@Override
	protected void populateComponentSpinner(IssueContainer.List components) {
		String[] componentOptions = new String[components.size() + 1];

		componentOptions[0] = getResources().getString(R.string.component_not_edit);
		int initialSelection = 0;
		int i = 1;
		for (IssueContainer c : components) {
			componentOptions[i] = c.getName();
			if (initialComponent.equals(c.getName())) {
				initialSelection = i;
			}
			i++;
		}

		initSpinner(componentSpinner, componentOptions, initialSelection);

		if (!components.isEmpty()) {
			componentText.setVisibility(View.VISIBLE);
			componentSpinner.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void populateVersionSpinner(IssueContainer.List versions) {
		String[] versionOptions = new String[versions.size() + 1];

		versionOptions[0] = getResources().getString(R.string.version_not_edit);
		int initialSelection = 0;
		int i = 1;
		for (IssueContainer v : versions) {
			versionOptions[i] = v.getName();
			if (initialVersion.equals(v.getName())) {
				initialSelection = i;
			}
			i++;
		}

		initSpinner(versionSpinner, versionOptions, initialSelection);

		if (!versions.isEmpty()) {
			versionText.setVisibility(View.VISIBLE);
			versionSpinner.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void populateMilestoneSpinner(IssueContainer.List milestones) {
		String[] milestoneOptions = new String[milestones.size() + 1];

		milestoneOptions[0] = getResources().getString(R.string.milestone_not_edit);
		int initialSelection = 0;
		int i = 1;
		for (IssueContainer ms : milestones) {
			milestoneOptions[i] = ms.getName();
			if (initialMilestone.equals(ms.getName())) {
				initialSelection = i;
			}
			i++;
		}

		initSpinner(milestonesSpinner, milestoneOptions, initialSelection);

		if (!milestones.isEmpty()) {
			milestoneText.setVisibility(View.VISIBLE);
			milestonesSpinner.setVisibility(View.VISIBLE);
		}
	}

	private void setInitialValues(Issue issue) {
		initialTitle = issue.getTitle();
		initialContent = issue.getContent();
		User responsible = issue.getResponsible();
		initialAssignee = (responsible == null ? "" : responsible.getUsername());
		initialKind = issue.getKind();
		initialPriority = issue.getPriority();
		initialStatus = issue.getStatus();

		initialMilestone = issue.getMetadata().getMilestone();
		initialComponent = issue.getMetadata().getComponent();
		initialVersion = issue.getMetadata().getVersion();

		// ensure not null
		if (initialMilestone == null)
			initialMilestone = "";

		if (initialComponent == null)
			initialComponent = "";

		if (initialVersion == null)
			initialVersion = "";
	}

	private void fillIssueForm(Issue issue) {
		titleEditor.setText(issue.getTitle());
		contentEditor.setText(issue.getContent());

		User responsible = issue.getResponsible();
		assigneeEditor.setText(responsible == null ? "" : responsible.getUsername());

		initSpinner(typeSpinner, Issue.Kind.class, new IssueKindResourceProvider(), initialKind);
		initSpinner(prioritySpinner, Issue.Priority.class, new IssuePriorityResourceProvider(), initialPriority);
		initSpinner(statusSpinner, Issue.Status.class, new IssueStatusResourceProvider(), initialStatus);
		viewAnimator.setDisplayedChild(1);
	}

	final class IssueRequestListener implements RequestListener<Issue> {
		@Override
		public void onRequestFailure(SpiceException spiceException) {
			showProgressBar(false);
		}

		@Override
		public void onRequestSuccess(Issue issue) {
			setInitialValues(issue);
			fillIssueForm(issue);
			requestMilestonesVersionsComponents();
		}
	}
}
