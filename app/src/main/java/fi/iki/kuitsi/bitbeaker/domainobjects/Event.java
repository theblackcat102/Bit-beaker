package fi.iki.kuitsi.bitbeaker.domainobjects;

import java.util.ArrayList;
import java.util.Date;

import fi.iki.kuitsi.bitbeaker.util.Objects;

/**
 * Represents an Event.
 */
public class Event {

	public static class List extends ArrayList<Event> {
	}

	Date utc_created_on;
	User user;
	String event;

	public Event(String event, User user, Date utc_created_on) {
		this.event = event;
		this.user = user;
		this.utc_created_on = utc_created_on;
	}

	public String getEventType() {
		return event;
	}

	public User getUser() {
		return user;
	}

	public Date getCreationDate() {
		return utc_created_on;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(utc_created_on, user, event);
	}
}
