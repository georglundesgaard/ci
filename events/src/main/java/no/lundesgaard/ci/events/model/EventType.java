package no.lundesgaard.ci.events.model;

public class EventType {
	private String name;

	public EventType(String name) {
		this.name = name;
	}

	public EventType() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
