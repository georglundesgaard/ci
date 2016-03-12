package no.lundesgaard.ci.events.resource;

import org.springframework.hateoas.ResourceSupport;

public class EventResource extends ResourceSupport {
	private String eventId;
	private String type;
	private String value;

	public EventResource(String eventId, String type, String value) {
		this.eventId = eventId;
		this.type = type;
		this.value = value;
	}

	public EventResource() {
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
