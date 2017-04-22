package fi.iki.kuitsi.bitbeaker.network.response.repositories;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class BranchNamesJsonAdapterTest {

	private Gson gson;

	@Before
	public void setUp() {
		gson = new Gson();
	}

	@Test
	public void fromJson() {
		JsonParser parser = new JsonParser();
		JsonElement e = parser.parse("{"
				+ "  \"default\": {"
				+ "    \"node\": \"8ed51420d940\","
				+ "    \"branch\": \"default\","
				+ "    \"revision\": 774,"
				+ "    \"size\": -1"
				+ "  },"
				+ "  \"maven_build\": {"
				+ "    \"node\": \"1364df410623\","
				+ "    \"parents\": ["
				+ "      \"03b8d45601e4\""
				+ "    ],"
				+ "    \"branch\": \"maven_build\","
				+ "    \"message\": \"add maven support\","
				+ "    \"revision\": 584,"
				+ "    \"size\": -1"
				+ "  }"
				+ "}");
		BranchNames branchNames = gson.fromJson(e, BranchNames.class);
		assertThat(branchNames).isNotNull();
		assertThat(branchNames).hasSize(2);
		assertThat(branchNames).containsAllOf("default", "maven_build").inOrder();
	}

	@Test
	public void toJson() {
		BranchNames branchNames = new BranchNames("default", "develop");
		String json = gson.toJson(branchNames);
		assertThat(json).isEqualTo("{\"default\":\"\",\"develop\":\"\"}");
	}
}
