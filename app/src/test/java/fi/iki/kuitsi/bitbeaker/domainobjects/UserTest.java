package fi.iki.kuitsi.bitbeaker.domainobjects;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class UserTest {

	@Test
	public void usersGetSortedInAscendingOrderByUsername() {
		User a = new User("alfa");
		User b = new User("BRAVO");
		User c = new User("Charlie");
		User d = new User("dElTa");
		List<User> users = Arrays.asList(c, b, d, a);
		Collections.sort(users);
		assertThat(users).containsExactly(a, b, c, d).inOrder();
	}
}
