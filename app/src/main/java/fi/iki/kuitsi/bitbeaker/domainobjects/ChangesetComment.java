package fi.iki.kuitsi.bitbeaker.domainobjects;

import java.util.ArrayList;
import java.util.Date;

/**
 * Represents a commented of a {@link Changeset}.
 */
public class ChangesetComment {

	public static class List extends ArrayList<ChangesetComment> {
	}

	private String username;
	private Date utc_created_on;
	private String content_rendered;
	private boolean deleted;

	public String getUsername() {
		return username;
	}

	public Date getCreationDate() {
		return utc_created_on;
	}

	public String getContent() {
		return content_rendered;
	}

	public boolean isDeleted() {
		return deleted;
	}
}
