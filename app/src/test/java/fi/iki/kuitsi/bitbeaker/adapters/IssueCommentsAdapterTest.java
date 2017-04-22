package fi.iki.kuitsi.bitbeaker.adapters;

import android.view.View;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Date;

import fi.iki.kuitsi.bitbeaker.BuildConfig;
import fi.iki.kuitsi.bitbeaker.domainobjects.IssueComment;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE, sdk = BuildConfig.ROBOLECTRIC_SDK)
public class IssueCommentsAdapterTest {

	@Test
	public void testAnonymousAuthor() throws Exception {
		IssueComment issueComment = new IssueComment("content", null, new Date(), new Date());
		IssueCommentsAdapter adapter = new IssueCommentsAdapter(RuntimeEnvironment.application, "user", "repo");
		adapter.add(issueComment);
		View view = adapter.getView(0, null, null);
		assertThat(view).isNotNull();
	}
}
