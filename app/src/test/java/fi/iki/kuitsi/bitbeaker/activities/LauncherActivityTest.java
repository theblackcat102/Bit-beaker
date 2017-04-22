package fi.iki.kuitsi.bitbeaker.activities;

import android.content.Intent;
import android.net.Uri;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import fi.iki.kuitsi.bitbeaker.ActivityComponent;
import fi.iki.kuitsi.bitbeaker.AppComponent;
import fi.iki.kuitsi.bitbeaker.AppComponentService;
import fi.iki.kuitsi.bitbeaker.BuildConfig;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.domainobjects.Changeset;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.BranchListRequest;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.ChangesetRequest;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.BranchNames;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.ActivityController;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, packageName="fi.iki.kuitsi.bitbeaker", sdk = BuildConfig.ROBOLECTRIC_SDK)
public class LauncherActivityTest {

	@Mock SpiceManager spiceManager;
	@Mock AppComponent appComponent;
	@Mock ActivityComponent.Builder activityComponentBuilder;
	@Mock ActivityComponent activityComponent;
	@Captor ArgumentCaptor<RequestListener<BranchNames>> branchNamesRequestListenerCaptor;
	@Captor ArgumentCaptor<RequestListener<Changeset>> changesetRequestListenerCaptor;

	private ActivityController<LauncherActivity> controller;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ShadowApplication.getInstance().setSystemService(AppComponentService.getServiceName(), appComponent);
		when(appComponent.activityComponentBuilder()).thenReturn(activityComponentBuilder);
		when(activityComponentBuilder.build()).thenReturn(activityComponent);
		when(activityComponent.spiceManager()).thenReturn(spiceManager);
		controller = Robolectric.buildActivity(LauncherActivity.class);
	}

	@Test
	public void launchChangesets() {
		controller.withIntent(createViewUri("https://bitbucket.org/testUser/testSlug/changeset/e5dc8a46ed"));
		ShadowActivity activity = shadowOf(controller.setup().get());

		assertThat(activity.isFinishing()).isFalse();
		verify(spiceManager, times(1)).execute(any(ChangesetRequest.class), any(), anyLong(),
				changesetRequestListenerCaptor.capture());
		RequestListener<Changeset> listener = changesetRequestListenerCaptor.getValue();
		assertThat(listener).isNotNull();
		// TODO: finish the test case; add constructor/builder to Changeset domain object, make it immutable
		//Intent result = getStartedActivityIntent();
		//assertNotNull(result);
		//assertNotNull(result.getComponent());
		//assertEquals(".activities.ChangesetActivity", result.getComponent().getShortClassName());
		//assertEquals("testUser", result.getStringExtra("owner"));
		//assertEquals("testSlug", result.getStringExtra("slug"));
	}

	@Test
	public void invalidChangest() {
		controller.withIntent(createViewUri("https://bitbucket.org/testUser/testSlug/changeset/10937512"));
		ShadowActivity activity = shadowOf(controller.setup().get());

		assertThat(activity.isFinishing()).isFalse();
		verify(spiceManager, times(1)).execute(any(ChangesetRequest.class), any(), anyLong(),
				changesetRequestListenerCaptor.capture());
		RequestListener<Changeset> listener = changesetRequestListenerCaptor.getValue();
		listener.onRequestFailure(new SpiceException("404"));
		assertThat(activity.isFinishing()).isTrue();
		Intent result = activity.getNextStartedActivity();
		assertThat(result).isNull();
	}

	@Test
	public void launchSourceCodeBrowser() {
		controller.withIntent(createViewUri("http://bitbucket.org/someFakeUser/someTestProject/src/"));
		ShadowActivity activity = shadowOf(controller.setup().get());

		assertThat(activity.isFinishing()).isFalse();
		verify(spiceManager, times(1)).execute(any(BranchListRequest.class), any(), anyLong(),
				branchNamesRequestListenerCaptor.capture());
		RequestListener<BranchNames> listener = branchNamesRequestListenerCaptor.getValue();
		BranchNames branchNames = new BranchNames("default");

		listener.onRequestSuccess(branchNames);
		Intent result = activity.getNextStartedActivity();

		assertThat(activity.isFinishing()).isTrue();
		assertThat(result.getComponent().getClassName()).endsWith(".activities.SourceBrowserActivity");
		assertThat(result.getStringExtra("owner")).isEqualTo("someFakeUser");
		assertThat(result.getStringExtra("slug")).isEqualTo("someTestProject");
		assertThat(result.getStringExtra("revision")).isEqualTo("default");
		assertThat(result.getStringArrayExtra("branches")).asList().containsExactly("default");
	}

	@Test
	public void launchRepository() {
		controller.withIntent(createViewUri("http://bitbucket.org/UserA/ProjectX/"));
		ShadowActivity activity = shadowOf(controller.create().get());
		Intent result = activity.getNextStartedActivity();

		assertThat(activity.isFinishing());
		assertThat(result.getComponent().getClassName()).endsWith(".activities.RepositoryActivity");
		assertThat(result.getStringExtra("owner")).isEqualTo("UserA");
		assertThat(result.getStringExtra("slug")).isEqualTo("ProjectX");
	}

	@Test
	public void test_repository_main_page_overview() {
		controller.withIntent(createViewUri("http://bitbucket.org/abc/def/overview"));
		ShadowActivity activity = shadowOf(controller.create().get());
		Intent result = activity.getNextStartedActivity();

		assertThat(activity.isFinishing());
		assertThat(result.getComponent().getClassName()).endsWith(".activities.RepositoryActivity");
		assertThat(result.getStringExtra("owner")).isEqualTo("abc");
		assertThat(result.getStringExtra("slug")).isEqualTo("def");
	}

	@Test
	public void launchUserProfile() {
		controller.withIntent(createViewUri("http://bitbucket.org/insertUsernameHere"));
		ShadowActivity activity = shadowOf(controller.create().get());
		Intent result = activity.getNextStartedActivity();

		assertThat(activity.isFinishing());
		assertThat(result.getComponent().getClassName()).endsWith(".activities.UserProfileActivity");
		assertThat(result.getStringExtra("user")).isEqualTo("insertUsernameHere");
	}

	@Test
	public void unknownLink() {
		controller.withIntent(createViewUri("http://bitbucket.org/myUser/myProject/unknown"));
		ShadowActivity activity = shadowOf(controller.create().get());
		Intent result = activity.getNextStartedActivity();

		assertThat(activity.isFinishing());
		assertThat(result).isNull();
		assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(
				RuntimeEnvironment.application.getString(R.string.link_not_supported));
	}

	@Test
	public void launchPullRequests() {
		controller.withIntent(createViewUri("http://bitbucket.org/someFakeUser/someTestProject/pull-requests/"));
		ShadowActivity activity = shadowOf(controller.create().get());
		Intent result = activity.getNextStartedActivity();

		assertThat(activity.isFinishing());
		assertThat(result.getComponent().getClassName()).endsWith(".activities.PullRequestActivity");
		assertThat(result.getStringExtra("owner")).isEqualTo("someFakeUser");
		assertThat(result.getStringExtra("slug")).isEqualTo("someTestProject");
	}

	@Test
	public void launchPullRequestComments() {
		controller.withIntent(createViewUri("http://bitbucket.org/someFakeUser/someTestProject/pull-request/1/pull-request-title"));
		ShadowActivity activity = shadowOf(controller.create().get());
		Intent result = activity.getNextStartedActivity();

		assertThat(activity.isFinishing());
		assertThat(result.getComponent().getClassName()).endsWith(".activities.PullRequestCommentActivity");
		assertThat(result.getStringExtra("owner")).isEqualTo("someFakeUser");
		assertThat(result.getStringExtra("slug")).isEqualTo("someTestProject");
		assertThat(result.getExtras().getInt("pullRequestId")).isEqualTo(1);
	}

	private static Intent createViewUri(String uriString) {
		Uri uri = Uri.parse(uriString);
		return new Intent(Intent.ACTION_VIEW, uri);
	}
}
