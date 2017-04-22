package fi.iki.kuitsi.bitbeaker.domainobjects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import fi.iki.kuitsi.bitbeaker.util.Objects;

/**
 * Represents a file within a {@link Changeset}.
 */
public class ChangesetFile implements Parcelable {

	public static class List extends ArrayList<ChangesetFile> {
	}

	public enum Type {
		@SerializedName("added")
		ADDED,
		@SerializedName("modified")
		MODIFIED,
		@SerializedName("removed")
		REMOVED;

		public static Type fromJsonType(String string) {
			return valueOf(string.toUpperCase());
		}

		@Override
		public String toString() {
			return this.name().toLowerCase();
		}
	}

	private String file;
	private Type type;

	public ChangesetFile(final String file, final Type type) {
		this.file = file;
		this.type = type;
	}

	private ChangesetFile(Parcel parcel) {
		this.file = parcel.readString();
		try {
			this.type = Type.fromJsonType(parcel.readString());
		} catch (IllegalArgumentException e) {
			this.type = Type.ADDED;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof ChangesetFile))
			return false;

		ChangesetFile rhs = (ChangesetFile) obj;
		return Objects.equal(file, rhs.file)
				&& Objects.equal(type, rhs.type);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(file, type);
	}

	public String getFile() {
		return file;
	}

	public Type getType() {
		return type;
	}

	/**
	 * Used to package this object into a {@link Parcel}.
	 *
	 * @param dest The {@link Parcel} to be written.
	 * @param flags The flags used for parceling.
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(file);
		if (type != null) {
			dest.writeString(type.toString());
		} else {
			dest.writeString("");
		}
	}

	/**
	 * Used to make this class parcelable.
	 */
	public static final Parcelable.Creator<ChangesetFile> CREATOR
			= new Parcelable.Creator<ChangesetFile>() {
		public ChangesetFile createFromParcel(Parcel parcel) {
			return new ChangesetFile(parcel);
		}

		public ChangesetFile[] newArray(int size) {
			return new ChangesetFile[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}
}
