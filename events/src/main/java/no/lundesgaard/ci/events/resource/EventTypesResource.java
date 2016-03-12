package no.lundesgaard.ci.events.resource;

import java.util.List;

import org.springframework.hateoas.ResourceSupport;

public class EventTypesResource extends ResourceSupport {
	private List<String> eventTypes;

	public EventTypesResource(List<String> eventTypes) {
		this.eventTypes = eventTypes;
	}

	public List<String> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(List<String> eventTypes) {
		this.eventTypes = eventTypes;
	}
}
