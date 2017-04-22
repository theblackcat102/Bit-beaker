package fi.iki.kuitsi.bitbeaker.domainobjects;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.JsonAdapter;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class ApiEnumTypeAdapterTest {

	private Gson gson;

	@Before
	public void setUp() {
		gson = new Gson();
	}

	@Test
	public void fromJson() {
		assertThat(gson.fromJson("\"value0\"", ClassWithEnum.Type.class))
				.isEqualTo(ClassWithEnum.Type.VALUE0);
	}

	@Test
	public void fromInvalidJson() {
		assertThat(gson.fromJson("\"value2\"", ClassWithEnum.Type.class))
				.isNull();
	}

	@Test
	public void toJson() {
		assertThat(gson.toJson(ClassWithEnum.Type.VALUE1))
				.isEqualTo("\"value1\"");
	}

	@Test
	public void completeClassFromJson() {
		JsonParser parser = new JsonParser();
		JsonElement e = parser.parse("{"
				+ "\"field1\": \"value0\","
				+ "\"field2\": \"value2\""
				+ "}");

		ClassWithEnum classWithEnum = gson.fromJson(e, ClassWithEnum.class);
		assertThat(classWithEnum).isNotNull();
		assertThat(classWithEnum.field1).isNotNull();
		assertThat(classWithEnum.field1).isEqualTo(ClassWithEnum.Type.VALUE0);
		assertThat(classWithEnum.field2).isNull();
	}

	static final class ClassWithEnum {
		@JsonAdapter(ApiEnumTypeAdapterFactory.class)
		enum Type implements ApiEnum {
			VALUE0("value0"),
			VALUE1("value1");

			private final String apiString;

			Type(String apiString) {
				this.apiString = apiString;
			}

			@Override @NonNull
			public String getApiString() {
				return apiString;
			}
		}

		Type field1;
		Type field2;
	}
}