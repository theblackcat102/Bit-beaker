package fi.iki.kuitsi.bitbeaker.domainobjects;

import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;

import java.util.Comparator;

public class RepositorySubject extends Subject<RepositorySubject, Repository> {

	private final Comparator<Repository> comparator;

	public RepositorySubject(FailureStrategy fs, Repository target,
			Comparator<Repository> comparator) {
		super(fs, target);
		this.comparator = comparator;
	}

	public final void comparesEqualTo(Repository other) {
		if (comparator.compare(actual(), other) != 0) {
			failWithRawMessage("%s should have been equivalent to <%s>", actualAsString(), other);
		}
	}

	public final void isLessThan(Repository other) {
		if (comparator.compare(actual(), other) >= 0) {
			fail("is less than", other);
		}
	}

	public final void isGreaterThan(Repository other) {
		if (comparator.compare(actual(), other) <= 0) {
			fail("is greater than", other);
		}
	}
}
