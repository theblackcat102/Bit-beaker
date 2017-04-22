package fi.iki.kuitsi.bitbeaker.preferences;


import com.google.common.truth.FailureStrategy;
import com.google.common.truth.SubjectFactory;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.fakes.RoboSharedPreferences;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.google.common.truth.Truth.assert_;

public class IntPreferenceTest {

	private static final String FILENAME = "filename";
	private static final IntPreferenceSubjectFactory INT_PREFERENCE_SUBJECT_FACTORY
			= new IntPreferenceSubjectFactory();
	private IntPreference intPreference;

	@Before
	public void setUp() {
		HashMap<String, Map<String, Object>> content = new HashMap<>();
		RoboSharedPreferences sharedPreferences = new RoboSharedPreferences(content, FILENAME,
				MODE_PRIVATE);
		intPreference = new IntPreference(sharedPreferences, "key", -1);
	}

	@Test
	public void returnKey() {
		assertThat(intPreference).keyEqualsTo("key");
	}

	@Test
	public void getDefault() {
		assertThat(intPreference).valueEqualsTo(-1);
	}

	@Test
	public void setValue() {
		intPreference.set(15);
		assertThat(intPreference).isSet();
		assertThat(intPreference).valueEqualsTo(15);
	}

	@Test
	public void notSetByDefault() {
		assertThat(intPreference).isNotSet();
	}

	@Test
	public void notSetAfterRemove() {
		intPreference.set(0);
		intPreference.remove();
		assertThat(intPreference).isNotSet();
	}

	@Test
	public void toStringTest() {
		intPreference.set(1);
		String s = intPreference.toString();
		assert_().that(s).startsWith("IntPreference");
		assert_().that(s).contains("key='key'");
		assert_().that(s).contains("value='1'");
		assert_().that(s).contains("defaultValue='-1'");
	}

	private static IntPreferenceSubject assertThat(IntPreference target) {
		return assert_().about(INT_PREFERENCE_SUBJECT_FACTORY).that(target);
	}

	static class IntPreferenceSubject extends AbstractPreferenceSubject<IntPreferenceSubject,
			Integer, IntPreference> {
		public IntPreferenceSubject(FailureStrategy failureStrategy, IntPreference subject) {
			super(failureStrategy, subject);
		}
	}

	static class IntPreferenceSubjectFactory extends SubjectFactory<IntPreferenceSubject,
			IntPreference> {
		@Override
		public IntPreferenceSubject getSubject(FailureStrategy fs, IntPreference that) {
			return new IntPreferenceSubject(fs, that);
		}
	}
}
