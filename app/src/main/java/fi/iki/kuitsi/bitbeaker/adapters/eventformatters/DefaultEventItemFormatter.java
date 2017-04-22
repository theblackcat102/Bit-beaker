package fi.iki.kuitsi.bitbeaker.adapters.eventformatters;

import android.content.Context;

import fi.iki.kuitsi.bitbeaker.Helper;
import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.domainobjects.Event;
import fi.iki.kuitsi.bitbeaker.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * Simple default event item formatter, used if no event specific formatter
 * is available.
 */
public class DefaultEventItemFormatter extends AbstractListItemFormatter<Event> {

	public DefaultEventItemFormatter(Context context, Event event) {
		super(context, event);
	}

	@Override
	public String getTitle() {
		String username = getItem().getUser().getUsername();
		Date date = getItem().getCreationDate();
		return Helper.formatDate(date) + (StringUtils.isBlank(username) ? "" : " - " + username);
	}

	@Override
	public String getSubtitle() {
		try {
			// Dynamically get the correct string from resources:
			Field stringId = R.string.class.getDeclaredField("event_type_" + getItem().getEventType());
			return getContext().getString(stringId.getInt(null));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// Failed, so just use the raw API string:
			return getItem().getEventType();
		} catch (Exception e) {
			e.printStackTrace();
			return "Details not available";
		}
	}
}
