package fi.iki.kuitsi.bitbeaker.adapters;

import org.junit.Test;

import fi.iki.kuitsi.bitbeaker.domainobjects.User;

import static com.google.common.truth.Truth.assertThat;
import static fi.iki.kuitsi.bitbeaker.adapters.RepositoryFollowersAdapter.getDisplayName;

/**
 * Unit test for {@link fi.iki.kuitsi.bitbeaker.adapters.RepositoryFollowersAdapter}.
 */
public class RepositoryFollowersAdapterTest {

	@Test public void showDisplayNameOfUser() {
		User user = new User("username");
		user.displayName("displayName");
		String displayName = getDisplayName(user);
		assertThat(displayName).isEqualTo(user.getDisplayName());
	}

	@Test public void howFirstNameAndLastNameOfUser() {
		User user = new User("username");
		user.firstName("firstName");
		user.lastName("lastName");
		String displayName = getDisplayName(user);
		assertThat(displayName).isEqualTo("firstName lastName");
	}

	@Test public void emptyName() {
		User user = new User("username");
		String displayName = getDisplayName(user);
		assertThat(displayName).isEmpty();
	}
}
