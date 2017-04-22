package fi.iki.kuitsi.bitbeaker.network.request.repositories;

import com.octo.android.robospice.persistence.DurationInMillis;

import fi.iki.kuitsi.bitbeaker.domainobjects.Event;
import fi.iki.kuitsi.bitbeaker.network.response.repositories.RepositoryEvents;
import fi.iki.kuitsi.bitbeaker.util.Objects;

/**
 * GET a list of events using `events` resource of `repositories` endpoint.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/events+Resources#eventsResources-GETalistofevents">events Resources - Bitbucket - Atlassian Documentation</a>
 */
public class EventRequest extends BaseRepositoriesRequest<Event.List> {

	/**
	 * An integer specifying the offset to start with. Can be null.
	 */
	private Integer start;
	/**
	 * An integer specifying the number of events to return. Can be null.
	 */
	private Integer limit;
	/**
	 * The event type to return. Can be null.
	 */
	private String type;

	/**
	 * Constructs a event list request associated with the specified repository.
	 * By default, this call returns the top 25 events.
	 *
	 * @param accountname The team or individual account owning the repo
	 * @param slug The repo identifier
	 */
	public EventRequest(String accountname, String slug) {
		super(Event.List.class, accountname, slug);
		init(null, null, null);
	}

	/**
	 * Constructs a event list request associated with the specified repository.
	 *
	 * @param accountname The team or individual account owning the repo
	 * @param slug The repo identifier
	 * @param limit An integer specifying the number of events to return
	 */
	public EventRequest(String accountname, String slug, int limit) {
		super(Event.List.class, accountname, slug);
		init(null, limit, null);
	}

	private void init(Integer start, Integer limit, String type) {
		this.start = start;
		this.limit = limit;
		this.type = type;
	}

	@Override
	public String getCacheKey() {
		return "events" + Integer.valueOf(hashCode());
	}

	@Override
	public long getCacheExpireDuration() {
		return 15 * DurationInMillis.ONE_MINUTE;
	}

	@Override
	public Event.List loadDataFromNetwork() throws Exception {
		RepositoryEvents events = getService().repositoryEventsRobospice(
				accountname, slug, start, limit, type).loadDataFromNetwork();
		return events.events;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(accountname, slug, start, limit);
	}
}
