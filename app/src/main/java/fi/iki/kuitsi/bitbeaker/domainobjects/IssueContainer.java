package fi.iki.kuitsi.bitbeaker.domainobjects;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.util.ArrayList;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Component, Milestone or Version in an issue tracker.
 */
public final class IssueContainer implements Comparable<IssueContainer> {

	@Retention(SOURCE)
	@StringDef({
			COMPONENT,
			MILESTONE,
			VERSION
	})
	public @interface Type { }
	public static final String COMPONENT = "component";
	public static final String MILESTONE = "milestone";
	public static final String VERSION = "version";

	private final String name;

	public IssueContainer(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public int compareTo(@NonNull IssueContainer other) {
		return name.compareToIgnoreCase(other.name);
	}

	public static class List extends ArrayList<IssueContainer> { }
}
