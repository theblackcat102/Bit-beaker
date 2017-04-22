package fi.iki.kuitsi.bitbeaker.preferences;

import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;

/**
 * A common supertype for Array subjects
 * @param <S>
 * @param <T>
 */
public class AbstractPreferenceSubject<S extends AbstractPreferenceSubject<S, T, C>, T, C extends AbstractPreference<T>>
		extends Subject<S, C> {

	public AbstractPreferenceSubject(FailureStrategy failureStrategy, C subject) {
		super(failureStrategy, subject);
	}

	public final void keyEqualsTo(String key) {
		if (!getSubject().getKey().equals(key)) {
			failWithRawMessage("%s key is wrong", getDisplaySubject());
		}
	}

	public final void valueEqualsTo(T value) {
		if (!getSubject().get().equals(value)) {
			failWithRawMessage("%s value is wrong", getDisplaySubject());
		}
	}

	public final void isSet() {
		if (!getSubject().isSet()) {
			failWithRawMessage("%s is not set", getDisplaySubject());
		}
	}

	public final void isNotSet() {
		if (getSubject().isSet()) {
			failWithRawMessage("%s is not set", getDisplaySubject());
		}
	}

}
