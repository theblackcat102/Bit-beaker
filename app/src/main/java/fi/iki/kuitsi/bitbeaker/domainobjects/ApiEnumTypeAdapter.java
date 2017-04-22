package fi.iki.kuitsi.bitbeaker.domainobjects;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

final class ApiEnumTypeAdapter<T extends Enum<T> & ApiEnum> extends TypeAdapter<T> {

	private final T[] enumConstants;

	public ApiEnumTypeAdapter(Class<T> classOfT) {
		enumConstants = classOfT.getEnumConstants();
	}

	@Override
	public T read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		String nextString = in.nextString();
		for (T constant : enumConstants) {
			if (constant.getApiString().equals(nextString))
				return constant;
		}
		return null;
	}

	@Override
	public void write(JsonWriter out, T value) throws IOException {
		out.value(value == null ? null : value.getApiString());
	}
}
