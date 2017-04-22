package fi.iki.kuitsi.bitbeaker.domainobjects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import fi.iki.kuitsi.bitbeaker.util.Objects;

/**
 * Represents a changeset.
 */
public class Changeset implements Parcelable {

	public static class List extends ArrayList<Changeset> {
	}

	private String node;
	private ChangesetFile.List files;
	private Date utctimestamp;
	private String author;
	private String raw_node;
	private String[] parents;
	private String branch;
	private String message;

	/**
	 * Create a new ChangeSet.
	 *
	 * @hide
	 */
	public Changeset() {
		files = new ChangesetFile.List();
		utctimestamp = new Date();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Changeset))
			return false;

		Changeset rhs = (Changeset) obj;
		return Objects.equal(node, rhs.node)
				&& Objects.equal(files, rhs.files)
				&& Objects.equal(utctimestamp, rhs.utctimestamp)
				&& Objects.equal(author, rhs.author)
				&& Objects.equal(raw_node, rhs.raw_node)
				&& Arrays.equals(parents, rhs.parents)
				&& Objects.equal(branch, rhs.branch)
				&& Objects.equal(message, rhs.message);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(
				node,
				files,
				utctimestamp,
				author,
				raw_node,
				parents,
				branch,
				message);
	}

	private Changeset(Parcel parcel) {
		this.node = parcel.readString();
		this.files = new ChangesetFile.List();
		parcel.readTypedList(this.files, ChangesetFile.CREATOR);
		this.utctimestamp = new Date(parcel.readLong());
		this.author = parcel.readString();
		this.raw_node = parcel.readString();
		this.parents = parcel.createStringArray();
		this.branch = parcel.readString();
		this.message = parcel.readString();
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getNode() {
		return node;
	}

	public void addFile(ChangesetFile file) {
		files.add(file);
	}

	public ChangesetFile.List getFiles() {
		return files;
	}

	public void setTimestamp(Date timestamp) {
		this.utctimestamp = timestamp;
	}

	public Date getTimestamp() {
		return utctimestamp;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAuthor() {
		return author;
	}

	public void setRawNode(String raw_node) {
		this.raw_node = raw_node;
	}

	public String getRawNode() {
		return raw_node;
	}

	public void setParents(String[] parents) {
		this.parents = parents;
	}

	public String[] getParents() {
		return parents;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getBranch() {
		return branch;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	/**
	 * Used to package this object into a {@link Parcel}.
	 *
	 * @param dest The {@link Parcel} to be written.
	 * @param flags The flags used for parceling.
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(node);
		if (files == null) {
			files = new ChangesetFile.List();
		}
		dest.writeTypedList(files);
		dest.writeLong(utctimestamp.getTime());
		dest.writeString(author);
		dest.writeString(raw_node);
		dest.writeStringArray(parents);
		dest.writeString(branch);
		dest.writeString(message);
	}

	/**
	 * Used to make this class parcelable.
	 */
	public static final Parcelable.Creator<Changeset> CREATOR
			= new Parcelable.Creator<Changeset>() {
		public Changeset createFromParcel(Parcel parcel) {
			return new Changeset(parcel);
		}

		public Changeset[] newArray(int size) {
			return new Changeset[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}
}
