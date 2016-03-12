package no.lundesgaard.ci.events.resource;

import org.springframework.hateoas.ResourceSupport;

public class EventsResource extends ResourceSupport {
	private int events;

	public EventsResource(int events) {
		this.events = events;
	}

	public int getEvents() {
		return events;
	}

	public void setEvents(int events) {
		this.events = events;
	}
}
