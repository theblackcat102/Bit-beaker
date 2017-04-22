package fi.iki.kuitsi.bitbeaker.domainobjects;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import fi.iki.kuitsi.bitbeaker.util.Objects;

public class User implements Comparable<User> {
	private String username;
	@SerializedName("first_name")
	private String firstName;
	@SerializedName("last_name")
	private String lastName;
	@SerializedName("display_name")
	private String displayName;
	@SerializedName("avatar")
	private String avatarUrl;
	@SerializedName("resource_uri")
	private String resourceUri;
	@SerializedName("is_team")
	private boolean team;

	public User(final String username) {
		this.username = username;
	}

	public String getResourceUri() {
		return resourceUri;
	}

	public User firstName(String first) {
		this.firstName = first;
		return this;
	}

	public User lastName(String last) {
		this.lastName = last;
		return this;
	}

	public User avatarUrl(String avatar) {
		this.avatarUrl = avatar;
		return this;
	}

	public User resourceUri(String resource) {
		this.resourceUri = resource;
		return this;
	}

	public User team(boolean isTeam) {
		this.team = isTeam;
		return this;
	}

	public User displayName(String displayName) {
		this.displayName = displayName;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public boolean isTeam() {
		return team;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public int compareTo(User other) {
		return username.compareToIgnoreCase(other.getUsername());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(username, displayName, avatarUrl);
	}

	public static class List extends ArrayList<User> {
	}
}
