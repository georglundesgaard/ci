package no.lundesgaard.ci.events.resource;

import org.springframework.hateoas.ResourceSupport;

public class EventTypeResource extends ResourceSupport {
	private String name;

	public EventTypeResource(String name) {
		this.name = name;
	}

	public EventTypeResource() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
