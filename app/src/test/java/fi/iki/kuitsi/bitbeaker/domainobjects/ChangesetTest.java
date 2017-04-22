package fi.iki.kuitsi.bitbeaker.domainobjects;

import android.os.Parcel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import fi.iki.kuitsi.bitbeaker.BuildConfig;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE, sdk = BuildConfig.ROBOLECTRIC_SDK)
public class ChangesetTest {

	@Test
	public void writeToParcelAndCrateFromParcel() {
		ChangesetFile file1 = new ChangesetFile("file1", ChangesetFile.Type.ADDED);
		ChangesetFile file2 = new ChangesetFile("file2", ChangesetFile.Type.MODIFIED);
		ChangesetFile file3 = new ChangesetFile("file3", ChangesetFile.Type.REMOVED);

		Changeset changeset = new Changeset();
		changeset.setAuthor("author");
		changeset.setMessage("message");
		changeset.setNode("node");
		changeset.setRawNode("raw_node");
		changeset.setBranch("branch");
		String[] parents = new String[1];
		parents[0] = "parent";
		changeset.setParents(parents);
		changeset.addFile(file1);
		changeset.addFile(file2);
		changeset.addFile(file3);

		// Obtain a Parcel object and write the parcelable object to it:
		Parcel parcel = Parcel.obtain();
		changeset.writeToParcel(parcel, 0);

		// After you're done with writing, you need to reset the parcel for reading:
		parcel.setDataPosition(0);

		// Reconstruct object from parcel and asserts:
		Changeset createdFromParcel = Changeset.CREATOR.createFromParcel(parcel);
		assertThat(createdFromParcel).isEqualTo(changeset);
	}
}
