package fi.iki.kuitsi.bitbeaker.fragments;

import android.os.Bundle;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import fi.iki.kuitsi.bitbeaker.adapters.EventsAdapter;
import fi.iki.kuitsi.bitbeaker.domainobjects.Event;
import fi.iki.kuitsi.bitbeaker.network.request.repositories.EventRequest;

/**
 * Fragment showing a list of repository events.
 */
public class EventListFragment extends SpiceListFragment {

	public static EventListFragment newInstance(String owner, String slug) {
		EventListFragment fragment = new EventListFragment();
		fragment.setArguments(buildArgumentsBundle(owner, slug));
		return fragment;
	}

	public static Bundle buildArgumentsBundle(String owner, String slug) {
		Bundle arguments = new Bundle();
		arguments.putString("owner", owner);
		arguments.putString("slug", slug);
		return arguments;
	}

	private EventsLoader loader;
	private EventsAdapter adapter;

	/**
	 * Mandatory empty constructor.
	 */
	public EventListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load arguments
		String owner = getArguments().getString("owner");
		String slug = getArguments().getString("slug");

		loader = new EventsLoader(owner, slug);
		adapter = new EventsAdapter(getActivity());
		setContentAdapter(adapter, loader);
	}

	private void populateList(Event.List events) {
		if (getActivity() != null) {
			if (adapter != null) {
				adapter.clear();
				adapter.addAll(events);
			}
		}
	}

	private class EventsLoader extends ContentLoader<Event.List> {

		private final EventRequest eventRequest;

		public EventsLoader(String owner, String slug) {
			eventRequest = new EventRequest(owner, slug);
		}

		@Override
		EventRequest getRequest() {
			return eventRequest;
		}

		@Override
		RequestListener<Event.List> getRequestListener() {
			return new EventRequestListener();
		}
	}

	private final class EventRequestListener implements RequestListener<Event.List> {
		@Override
		public void onRequestFailure(SpiceException e) {
			loader.notifyFinished(e);
		}

		@Override
		public void onRequestSuccess(final Event.List events) {
			populateList(events);
			loader.notifyFinished();
		}
	}
}
