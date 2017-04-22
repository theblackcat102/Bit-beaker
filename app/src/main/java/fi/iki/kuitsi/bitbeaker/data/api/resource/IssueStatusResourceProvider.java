package fi.iki.kuitsi.bitbeaker.data.api.resource;

import java.util.EnumMap;
import java.util.Map;

import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.domainobjects.Issue;

public final class IssueStatusResourceProvider implements StringProvider<Issue.Status> {

	private static final Map<Issue.Status, Integer> STATUS_TO_STRING_RESOURCE;

	static {
		STATUS_TO_STRING_RESOURCE = new EnumMap<>(Issue.Status.class);
		STATUS_TO_STRING_RESOURCE.put(Issue.Status.NEW, R.string.api_status_new);
		STATUS_TO_STRING_RESOURCE.put(Issue.Status.OPEN, R.string.api_status_open);
		STATUS_TO_STRING_RESOURCE.put(Issue.Status.ON_HOLD, R.string.api_status_on_hold);
		STATUS_TO_STRING_RESOURCE.put(Issue.Status.DUPLICATE, R.string.api_status_duplicate);
		STATUS_TO_STRING_RESOURCE.put(Issue.Status.INVALID, R.string.api_status_invalid);
		STATUS_TO_STRING_RESOURCE.put(Issue.Status.WONTFIX, R.string.api_status_wontfix);
		STATUS_TO_STRING_RESOURCE.put(Issue.Status.RESOLVED, R.string.api_status_resolved);
		STATUS_TO_STRING_RESOURCE.put(Issue.Status.CLOSED, R.string.api_status_closed);
	}

	@Override
	public int getStringRes(Issue.Status input) {
		return STATUS_TO_STRING_RESOURCE.get(input);
	}
}
