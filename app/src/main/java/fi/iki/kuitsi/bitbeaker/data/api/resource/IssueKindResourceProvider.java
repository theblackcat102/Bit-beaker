package fi.iki.kuitsi.bitbeaker.data.api.resource;

import java.util.EnumMap;
import java.util.Map;

import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.domainobjects.Issue;

public final class IssueKindResourceProvider implements StringProvider<Issue.Kind>,
		DrawableProvider<Issue.Kind> {

	private static final Map<Issue.Kind, Integer> KIND_TO_STRING_RESOURCE;

	static {
		KIND_TO_STRING_RESOURCE = new EnumMap<>(Issue.Kind.class);
		KIND_TO_STRING_RESOURCE.put(Issue.Kind.BUG, R.string.api_kind_bug);
		KIND_TO_STRING_RESOURCE.put(Issue.Kind.ENHANCEMENT, R.string.api_kind_enhancement);
		KIND_TO_STRING_RESOURCE.put(Issue.Kind.PROPOSAL, R.string.api_kind_proposal);
		KIND_TO_STRING_RESOURCE.put(Issue.Kind.TASK, R.string.api_kind_task);
	}

	private static final Map<Issue.Kind, Integer> KIND_TO_DRAWABLE_RESOURCE;

	static {
		KIND_TO_DRAWABLE_RESOURCE = new EnumMap<>(Issue.Kind.class);
		KIND_TO_DRAWABLE_RESOURCE.put(Issue.Kind.BUG, R.drawable.icon_bug);
		KIND_TO_DRAWABLE_RESOURCE.put(Issue.Kind.ENHANCEMENT, R.drawable.icon_enhancement);
		KIND_TO_DRAWABLE_RESOURCE.put(Issue.Kind.PROPOSAL, R.drawable.icon_proposal);
		KIND_TO_DRAWABLE_RESOURCE.put(Issue.Kind.TASK, R.drawable.icon_task);
	}

	@Override
	public int getStringRes(Issue.Kind input) {
		return KIND_TO_STRING_RESOURCE.get(input);
	}

	@Override
	public int getDrawableRes(Issue.Kind input) {
		return KIND_TO_DRAWABLE_RESOURCE.get(input);
	}
}
