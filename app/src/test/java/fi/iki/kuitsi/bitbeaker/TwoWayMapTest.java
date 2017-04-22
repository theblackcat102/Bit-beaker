package fi.iki.kuitsi.bitbeaker;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class TwoWayMapTest {

	@Test
	public void PutAndGetWithStringAndInteger() {
		TwoWayMap<String, Integer> instance = new TwoWayMap<>();
		instance.put("key1", 11);
		instance.put("key2", 22);

		assertThat(instance.getByKey("key1")).isEqualTo(11);
		assertThat(instance.getByValue(11)).isEqualTo("key1");

		assertThat(instance.getByKey("key2")).isEqualTo(22);
		assertThat(instance.getByValue(22)).isEqualTo("key2");
	}

	@Test
	public void DefaultValuesWithStringAndInteger() {
		// Test that null is the default default value:
		TwoWayMap<String, Integer> instance = new TwoWayMap<>();
		assertThat(instance.getByKey("nonexistent")).isNull();
		assertThat(instance.getByValue(1234)).isNull();

		// Test default values with an empty map:
		instance = new TwoWayMap<>("default key", 999);
		assertThat(instance.getByKey("nonexistent")).isEqualTo(999);
		assertThat(instance.getByValue(42)).isEqualTo("default key");

		// Put some data into the map, then test the defaults again:
		instance.put("a", 1);
		instance.put("b", 2);
		instance.put("c", 3);
		assertThat(instance.getByKey("nonexistent")).isEqualTo(999);
		assertThat(instance.getByValue(42)).isEqualTo("default key");

		// Test that the put commands also succeeded:
		assertThat(instance.getByKey("a")).isEqualTo(1);
		assertThat(instance.getByKey("b")).isEqualTo(2);
		assertThat(instance.getByKey("c")).isEqualTo(3);
		assertThat(instance.getByValue(1)).isEqualTo("a");
		assertThat(instance.getByValue(2)).isEqualTo("b");
		assertThat(instance.getByValue(3)).isEqualTo("c");
	}

	@Test
	public void OverridingValues() {
		TwoWayMap<String, String> instance = new TwoWayMap<>();

		// Put something into the map and assert that it worked:
		instance.put("key1", "old value 1");
		assertThat(instance.getByKey("key1")).isEqualTo("old value 1");
		assertThat(instance.getByValue("old value 1")).isEqualTo("key1");

		// Overwrite the value and assert that everything worked:
		instance.put("key1", "new value 1");
		assertThat(instance.getByKey("key1")).isEqualTo("new value 1");
		assertThat(instance.getByValue("old value 1")).isNull();
		assertThat(instance.getByValue("new value 1")).isEqualTo("key1");
	}

	@Test
	public void DefaultValuesAlsoInTheMap() {
		TwoWayMap<String, String> instance = new TwoWayMap<>("KEY", "VALUE");
		assertThat(instance.getByKey("anything")).isEqualTo("VALUE");
		assertThat(instance.getByValue("something")).isEqualTo("KEY");

		assertThat(instance.getByKey("KEY")).isEqualTo("VALUE");
		assertThat(instance.getByValue("VALUE")).isEqualTo("KEY");

		instance.put("KEY", "the actual value of key");
		assertThat(instance.getByKey("KEY")).isEqualTo("the actual value of key");
		assertThat(instance.getByValue("the actual value of key")).isEqualTo("KEY");
		assertThat(instance.getByValue("anything else")).isEqualTo("KEY");
		assertThat(instance.getByValue("VALUE")).isEqualTo("KEY");

		instance.put("the actual key", "VALUE");
		assertThat(instance.getByKey("the actual key")).isEqualTo("VALUE");
		assertThat(instance.getByValue("VALUE")).isEqualTo("the actual key");
	}
}