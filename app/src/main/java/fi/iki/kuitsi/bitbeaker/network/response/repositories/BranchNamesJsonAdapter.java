package fi.iki.kuitsi.bitbeaker.network.response.repositories;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * {@link TypeAdapter} for {@link BranchNames}.
 */
class BranchNamesJsonAdapter extends TypeAdapter<BranchNames> {

	@Override
	public void write(JsonWriter out, BranchNames value) throws IOException {
		out.beginObject();
		for (String name : value) {
			out.name(name).value("");
		}
		out.endObject();
	}

	@Override
	public BranchNames read(JsonReader in) throws IOException {
		BranchNames branchNames = new BranchNames();
		in.beginObject();
		while (in.hasNext()) {
			branchNames.add(in.nextName());
			in.skipValue();
		}
		in.endObject();
		return branchNames;
	}
}
