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

public class StringPreferenceTest {

	private static final String FILENAME = "filename";
	private static final IntPreferenceSubjectFactory STRING_PREFERENCE_SUBJECT_FACTORY
			= new IntPreferenceSubjectFactory();
	private StringPreference stringPreference;

	@Before
	public void setUp() {
		HashMap<String, Map<String, Object>> content = new HashMap<>();
		RoboSharedPreferences sharedPreferences = new RoboSharedPreferences(content, FILENAME,
				MODE_PRIVATE);
		stringPreference = new StringPreference(sharedPreferences, "key", "default");
	}

	@Test
	public void returnKey() {
		assertThat(stringPreference).keyEqualsTo("key");
	}

	@Test
	public void getDefault() {
		assertThat(stringPreference).valueEqualsTo("default");
	}

	@Test
	public void setValue() {
		stringPreference.set("value");
		assertThat(stringPreference).isSet();
		assertThat(stringPreference).valueEqualsTo("value");
	}

	@Test
	public void notSetByDefault() {
		assertThat(stringPreference).isNotSet();
	}

	@Test
	public void notSetAfterRemove() {
		stringPreference.set("value");
		stringPreference.remove();
		assertThat(stringPreference).isNotSet();
	}

	@Test
	public void toStringTest() {
		stringPreference.set("value");
		String s = stringPreference.toString();
		assert_().that(s).startsWith("StringPreference");
		assert_().that(s).contains("key='key'");
		assert_().that(s).contains("value='value'");
		assert_().that(s).contains("defaultValue='default'");
	}

	private static StringPreferenceSubject assertThat(StringPreference target) {
		return assert_().about(STRING_PREFERENCE_SUBJECT_FACTORY).that(target);
	}

	static class StringPreferenceSubject extends AbstractPreferenceSubject<StringPreferenceSubject,
			String, StringPreference> {
		public StringPreferenceSubject(FailureStrategy failureStrategy, StringPreference subject) {
			super(failureStrategy, subject);
		}
	}

	static class IntPreferenceSubjectFactory extends SubjectFactory<StringPreferenceSubject,
			StringPreference> {
		@Override
		public StringPreferenceSubject getSubject(FailureStrategy fs, StringPreference that) {
			return new StringPreferenceSubject(fs, that);
		}
	}
}
