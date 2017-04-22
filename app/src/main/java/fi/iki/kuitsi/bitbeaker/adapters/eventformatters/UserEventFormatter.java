package fi.iki.kuitsi.bitbeaker.adapters.eventformatters;

import android.content.Context;
import android.view.View.OnClickListener;

import fi.iki.kuitsi.bitbeaker.clicklisteners.UserProfileActivityStartingClickListener;
import fi.iki.kuitsi.bitbeaker.domainobjects.Event;

public class UserEventFormatter extends DefaultEventItemFormatter {

	public UserEventFormatter(Context context, Event event) {
		super(context, event);
	}

	@Override
	public OnClickListener getClickListener() {
		try {
			final String user = getItem().getUser().getUsername();
			return new UserProfileActivityStartingClickListener(user);
		} catch (Exception e) {
			// The user may be null, which is when we end up here.
			return null;
		}
	}
}
