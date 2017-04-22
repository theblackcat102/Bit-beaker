package fi.iki.kuitsi.bitbeaker.network.response.repositories;

import java.util.List;

import fi.iki.kuitsi.bitbeaker.domainobjects.Event;

/**
 * Response of GET a list of events
 * using `events` resource of `repositories` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/events+Resources#eventsResources-GETalistofevents">events Resources - Bitbucket - Atlassian Documentation</a>
 */
public class RepositoryEvents {

	/**
	 * If the type parameter is specified in the request, it contains the total number of events of
	 * that type associated with the account.
	 */
	public final int count;

	/**
	 * List of {@link Event}s.
	 */
	public final Event.List events;

	public RepositoryEvents(List<Event> events) {
		this.count = events.size();
		this.events = new Event.List();
		this.events.addAll(events);
	}
}
