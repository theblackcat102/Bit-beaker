package fi.iki.kuitsi.bitbeaker.domainobjects;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;

/**
 * Represent a pull request.
 */
public class PullRequestComment {

	public static class List extends ArrayList<PullRequestComment> {
	}

	@SerializedName("utc_created_on")
	private Date created;
	@SerializedName("utc_last_updated")
	private Date updated;
	private String content;
	private String contentRendered;
	@SerializedName("author_info")
	private User author;
	private boolean deleted;
	private boolean isSpam;

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContentRendered() {
		return contentRendered;
	}

	public void setContentRendered(String contentRendered) {
		this.contentRendered = contentRendered;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isSpam() {
		return isSpam;
	}

	public void setSpam(boolean isSpam) {
		this.isSpam = isSpam;
	}
}
