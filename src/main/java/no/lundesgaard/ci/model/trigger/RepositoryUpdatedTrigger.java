package no.lundesgaard.ci.model.trigger;

import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class RepositoryUpdatedTrigger implements Trigger {
	public final String repository;

	public RepositoryUpdatedTrigger(String repository) {
		this.repository = repository;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("repository", repository)
				.toString();
	}
}
