package fi.iki.kuitsi.bitbeaker.data;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static fi.iki.kuitsi.bitbeaker.data.GlideImageLoader.rewriteAvatarUrl;

public class GlideImageLoaderTest {

	@Test public void modifySize() {
		assertThat(rewriteAvatarUrl("https://bitbucket.org/account/user/avatar/32?ts=0", 80))
				.isEqualTo("https://bitbucket.org/account/user/avatar/80?ts=0");
	}
}
