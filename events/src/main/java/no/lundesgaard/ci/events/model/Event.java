package no.lundesgaard.ci.events.model;

public class Event {
	private String id;
	private String type;
	private String value;

	public Event(String type, String value) {
		this.type = type;
		this.value = value;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
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
