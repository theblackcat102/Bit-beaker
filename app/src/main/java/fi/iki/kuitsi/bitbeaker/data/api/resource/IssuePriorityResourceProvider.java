package fi.iki.kuitsi.bitbeaker.data.api.resource;

import java.util.EnumMap;
import java.util.Map;

import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.domainobjects.Issue;

public final class IssuePriorityResourceProvider implements StringProvider<Issue.Priority> {

	private static final Map<Issue.Priority, Integer> PRIORITY_TO_STRING_RESOURCE;

	static {
		PRIORITY_TO_STRING_RESOURCE = new EnumMap<>(Issue.Priority.class);
		PRIORITY_TO_STRING_RESOURCE.put(Issue.Priority.TRIVIAL, R.string.api_priority_trivial);
		PRIORITY_TO_STRING_RESOURCE.put(Issue.Priority.MINOR, R.string.api_priority_minor);
		PRIORITY_TO_STRING_RESOURCE.put(Issue.Priority.MAJOR, R.string.api_priority_major);
		PRIORITY_TO_STRING_RESOURCE.put(Issue.Priority.CRITICAL, R.string.api_priority_critical);
		PRIORITY_TO_STRING_RESOURCE.put(Issue.Priority.BLOCKER, R.string.api_priority_blocker);
	}

	@Override
	public int getStringRes(Issue.Priority input) {
		return PRIORITY_TO_STRING_RESOURCE.get(input);
	}
}
