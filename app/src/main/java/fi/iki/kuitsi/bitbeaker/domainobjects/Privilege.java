package fi.iki.kuitsi.bitbeaker.domainobjects;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Privilege {

	public enum PrivilegeType {
		@SerializedName("admin")
		ADMIN,
		@SerializedName("write")
		WRITE,
		@SerializedName("read")
		READ
	}

	private String repo;
	@SerializedName("privilege")
	private PrivilegeType privilege;

	public String getRepo() {
		return this.repo;
	}

	public PrivilegeType getPrivilege() {
		return this.privilege;
	}

	public static class List extends ArrayList<Privilege> {
	}

}
