package fi.iki.kuitsi.bitbeaker.data.api.resource;

import org.junit.Before;
import org.junit.Test;

import fi.iki.kuitsi.bitbeaker.domainobjects.Issue;

import static com.google.common.truth.Truth.assertThat;

public class IssueKindResourceProviderTest {

	IssueKindResourceProvider provider = new IssueKindResourceProvider();

	@Before
	public void setUp() {
		provider = new IssueKindResourceProvider();
	}

	@Test
	public void stringRes() {
		for (Issue.Kind kind : Issue.Kind.values()) {
			assertThat(provider.getStringRes(kind)).isNotEqualTo(0);
		}
	}
}
