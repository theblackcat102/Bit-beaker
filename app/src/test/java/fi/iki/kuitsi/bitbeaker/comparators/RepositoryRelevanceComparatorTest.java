package fi.iki.kuitsi.bitbeaker.comparators;

import com.google.common.truth.FailureStrategy;
import com.google.common.truth.SubjectFactory;

import org.junit.Test;

import java.util.Comparator;
import java.util.Date;

import fi.iki.kuitsi.bitbeaker.domainobjects.Repository;
import fi.iki.kuitsi.bitbeaker.domainobjects.RepositorySubject;

import static com.google.common.truth.Truth.assert_;

public class RepositoryRelevanceComparatorTest {
	private static final Date NEWER = new Date(99999999L);
	private static final Date OLDER = new Date(4444L);
	private static final Repository ORIGINAL_A = new Repository("owner", "slug").lastUpdated(NEWER);
	private static final Repository ORIGINAL_B = new Repository("owner", "slug").lastUpdated(OLDER);
	private static final Repository FORK_A = new Repository("owner", "slug").lastUpdated(NEWER).forkOf(ORIGINAL_A);
	private static final Repository FORK_B = new Repository("owner", "slug").lastUpdated(OLDER).forkOf(ORIGINAL_A);
	private static final Comparator<Repository> COMPARATOR = new RepositoryRelevanceComparator();

	private static final SubjectFactory<RepositorySubject, Repository> REPOSITORY_SUBJECT_FACTORY =
			new SubjectFactory<RepositorySubject, Repository>() {
				@Override
				public RepositorySubject getSubject(FailureStrategy fs, Repository target) {
					return new RepositorySubject(fs, target, COMPARATOR);
				}
			};

	@Test
	public void test_non_forked_repositories_get_in_last_updated_order() {
		assertThat(ORIGINAL_A).isLessThan(ORIGINAL_B);
		assertThat(ORIGINAL_B).isGreaterThan(ORIGINAL_A);
	}

	@Test
	public void test_forked_repositories_get_in_last_updated_order() {
		assertThat(FORK_A).isLessThan(FORK_B);
		assertThat(FORK_B).isGreaterThan(FORK_A);
	}

	@Test
	public void test_forked_repository_comes_after_original_repository() {
		assertThat(FORK_A).isGreaterThan(ORIGINAL_A);
		assertThat(FORK_B).isGreaterThan(ORIGINAL_A);
		assertThat(FORK_A).isGreaterThan(ORIGINAL_B);
		assertThat(FORK_B).isGreaterThan(ORIGINAL_B);
		assertThat(ORIGINAL_A).isLessThan(FORK_A);
		assertThat(ORIGINAL_A).isLessThan(FORK_B);
		assertThat(ORIGINAL_B).isLessThan(FORK_A);
		assertThat(ORIGINAL_B).isLessThan(FORK_B);
	}

	@Test
	public void test_repositories_with_same_attributes_rank_identically() {
		assertThat(FORK_A).comparesEqualTo(FORK_A);
		assertThat(FORK_B).comparesEqualTo(FORK_B);
		assertThat(ORIGINAL_A).comparesEqualTo(ORIGINAL_A);
		assertThat(ORIGINAL_B).comparesEqualTo(ORIGINAL_B);
	}

	private static RepositorySubject assertThat(Repository target) {
		return assert_().about(REPOSITORY_SUBJECT_FACTORY).that(target);
	}

}
