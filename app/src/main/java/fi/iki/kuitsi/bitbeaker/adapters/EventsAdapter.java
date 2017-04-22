package fi.iki.kuitsi.bitbeaker.adapters;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fi.iki.kuitsi.bitbeaker.R;
import fi.iki.kuitsi.bitbeaker.adapters.eventformatters.DefaultEventItemFormatter;
import fi.iki.kuitsi.bitbeaker.adapters.eventformatters.ListItemFormatter;
import fi.iki.kuitsi.bitbeaker.adapters.eventformatters.UserEventFormatter;
import fi.iki.kuitsi.bitbeaker.domainobjects.Event;

/**
 * This adapter is used to create a list of events, as received from the Bitbucket API.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/events+Resources">events Resources - Bitbucket - Atlassian Documentation</a>
 */
public class EventsAdapter extends ParameterizedAdapter<Event> {

	private static Map<String, Class<? extends ListItemFormatter>> formatters
			= new HashMap<String, Class<? extends ListItemFormatter>>();

	static {
		formatters.put("start_follow_repo", UserEventFormatter.class);
		formatters.put("stop_follow_repo", UserEventFormatter.class);
	}

	public EventsAdapter(Context context) {
		super(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem_two_rows, parent, false);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.subtitle = (TextView) convertView.findViewById(R.id.subtitle);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final Event event = getItem(position);

		// Find the correct formatter for this event type:
		try {
			Class<? extends ListItemFormatter> c = formatters.get(event.getEventType());
			if (c == null) {
				c = DefaultEventItemFormatter.class;
			}
			Constructor<? extends ListItemFormatter> formatterConstructor = c.getDeclaredConstructor(Context.class, Event.class);
			ListItemFormatter formatter = formatterConstructor.newInstance(getContext(), getItem(position));

			holder.title.setText(formatter.getTitle());
			holder.subtitle.setText(formatter.getSubtitle());
			convertView.setOnClickListener(formatter.getClickListener());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return convertView;
	}

	private static class ViewHolder {
		TextView title;
		TextView subtitle;
	}
}
