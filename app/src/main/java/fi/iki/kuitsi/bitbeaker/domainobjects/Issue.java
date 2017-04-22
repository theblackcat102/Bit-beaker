package fi.iki.kuitsi.bitbeaker.domainobjects;

import android.support.annotation.NonNull;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;

public final class Issue implements Comparable<Issue> {

	@JsonAdapter(ApiEnumTypeAdapterFactory.class)
	public enum Priority implements ApiEnum {
		TRIVIAL("trivial"),
		MINOR("minor"),
		MAJOR("major"),
		CRITICAL("critical"),
		BLOCKER("blocker");

		private final String apiString;

		Priority(String apiString) {
			this.apiString = apiString;
		}

		@Override
		public String toString() {
			return apiString;
		}

		@Override @NonNull
		public String getApiString() {
			return apiString;
		}
	}

	@JsonAdapter(ApiEnumTypeAdapterFactory.class)
	public enum Status implements ApiEnum {
		NEW("new"),
		OPEN("open"),
		ON_HOLD("on hold"),
		DUPLICATE("duplicate"),
		INVALID("invalid"),
		WONTFIX("wontfix"),
		RESOLVED("resolved"),
		CLOSED("closed");

		private final String apiString;

		Status(String apiString) {
			this.apiString = apiString;
		}

		@Override
		public String toString() {
			return apiString;
		}

		@Override @NonNull
		public String getApiString() {
			return apiString;
		}
	}

	@JsonAdapter(ApiEnumTypeAdapterFactory.class)
	public enum Kind implements ApiEnum {
		BUG("bug"),
		ENHANCEMENT("enhancement"),
		PROPOSAL("proposal"),
		TASK("task");

		private final String apiString;

		Kind(String apiString) {
			this.apiString = apiString;
		}

		@Override
		public String toString() {
			return apiString;
		}

		@Override @NonNull
		public String getApiString() {
			return apiString;
		}
	}

	private String title;
	private Status status;
	private Priority priority;
	@SerializedName("utc_last_updated")
	private java.util.Date lastUpdated;
	@SerializedName("comment_count")
	private int commentCount;
	private Issue.Metadata metadata;
	private String content;
	@SerializedName("local_id")
	private int localId;
	@SerializedName("follower_count")
	private int followerCount;
	@SerializedName("utc_created_on")
	private java.util.Date createdOn;
	@SerializedName("resource_uri")
	private String resourceUri;
	@SerializedName("is_spam")
	private boolean spam;
	@SerializedName("reported_by")
	private User reportedBy;
	@SerializedName("responsible")
	private User responsible;

	public String getTitle() {
		return title;
	}

	public Status getStatus() {
		return status;
	}

	public Priority getPriority() {
		return priority;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public Issue commentCount(int commentCount) {
		this.commentCount = commentCount;
		return this;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public Kind getKind() {
		return metadata.getKind();
	}

	public String getContent() {
		return content;
	}

	public int getLocalId() {
		return localId;
	}

	public int getFollowerCount() {
		return followerCount;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public String getResourceUri() {
		return resourceUri;
	}

	public boolean isSpam() {
		return spam;
	}

	public User getReportedBy() {
		return reportedBy;
	}

	public User getResponsible() {
		return responsible;
	}

	@Override
	public int compareTo(@NonNull Issue other) {
		return title.compareToIgnoreCase(other.getTitle());
	}

	public static class List extends ArrayList<Issue> {
	}

	public static class Metadata {

		private final Kind kind;
		private final String version;
		private final String component;
		private final String milestone;

		public Metadata(Kind kind, String version, String component, String milestone) {
			this.kind = kind;
			this.version = version;
			this.component = component;
			this.milestone = milestone;
		}

		public Kind getKind() {
			return kind;
		}

		public String getVersion() {
			return version;
		}

		public String getComponent() {
			return component;
		}

		public String getMilestone() {
			return milestone;
		}
	}
}
