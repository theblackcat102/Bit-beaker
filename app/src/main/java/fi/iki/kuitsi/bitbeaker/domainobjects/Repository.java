package fi.iki.kuitsi.bitbeaker.domainobjects;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;

/**
 * Represent a repository.
 */
public class Repository implements Comparable<Repository> {
	private final String owner;
	private final String slug;
	private String name;
	private Repository forkOf;
	@SerializedName("is_private")
	private boolean privateRepository;
	private String description;
	@SerializedName("utc_last_updated")
	private Date lastUpdated;
	private String logo;
	private boolean has_wiki;
	private boolean has_issues;
	private String website;
	private String scm;

	public boolean isPrivateRepository() {
		return privateRepository;
	}

	public Repository(final String owner, final String slug) {
		this.owner = owner;
		this.slug = slug;
	}

	public Repository name(String repoName) {
		this.name = repoName;
		return this;
	}

	public Repository forkOf(Repository parentProject) {
		this.forkOf = parentProject;
		return this;
	}

	public Repository privateRepository(boolean privateRepo) {
		this.privateRepository = privateRepo;
		return this;
	}

	public Repository description(String desc) {
		this.description = desc;
		return this;
	}

	public Repository lastUpdated(Date updated) {
		this.lastUpdated = updated;
		return this;
	}

	public Repository scm(String scm) {
		this.scm = scm;
		return this;
	}

	public String getOwner() {
		return owner;
	}

	public String getSlug() {
		return slug;
	}

	public String getName() {
		return name;
	}

	public Repository getForkOf() {
		return forkOf;
	}

	public String getDisplayName() {
		return (name != null ? name : slug);
	}

	public String getDescription() {
		return description;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public boolean isFork() {
		return forkOf != null;
	}

	@Override
	public int compareTo(Repository other) {
		return name.compareToIgnoreCase(other.name);
	}

	@Override
	public String toString() {
		return owner + "/" + slug;
	}

	public static class List extends ArrayList<Repository> {
	}

	public String getLogo() {
		return logo;
	}

	public boolean hasWiki() {
		return has_wiki;
	}

	public boolean hasIssues() {
		return has_issues;
	}

	public String getWebsite() {
		return website;
	}

	public String getScm() {
		return scm;
	}

	@Override
	public boolean equals(Object o) {
		// Auto-generated equals()
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Repository that = (Repository) o;

		if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
		return !(slug != null ? !slug.equals(that.slug) : that.slug != null);

	}

	@Override
	public int hashCode() {
		// Auto-generated hashCode()
		int result = owner != null ? owner.hashCode() : 0;
		result = 31 * result + (slug != null ? slug.hashCode() : 0);
		return result;
	}
}
