package fi.iki.kuitsi.bitbeaker.domainobjects;

import java.util.ArrayList;
import java.util.Date;

/**
 * Represents a comment of an {@link Issue}.
 */
public class IssueComment {

	private final String content;
	private final User authorInfo;
	private final Date utcCreatedOn;
	private final Date utcUpdatedOn;

	public IssueComment(String content, User author, Date utcCreatedOn, Date utcUpdatedOn) {
		this.content = content;
		this.authorInfo = author;
		this.utcCreatedOn = utcCreatedOn;
		this.utcUpdatedOn = utcUpdatedOn;
	}

	public String getContent() {
		return content;
	}

	public User getAuthor() {
		return authorInfo;
	}

	public Date getUtcCreatedOn() {
		return utcCreatedOn;
	}

	public Date getUtcUpdatedOn() {
		return utcUpdatedOn;
	}

	public static class List extends ArrayList<IssueComment> {
	}

}
