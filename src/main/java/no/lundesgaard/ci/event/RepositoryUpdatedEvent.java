package no.lundesgaard.ci.event;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class RepositoryUpdatedEvent implements Event {
	private final String name;

	public RepositoryUpdatedEvent(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("name", name)
				.toString();
	}
}
