package no.lundesgaard.ci.event;

import no.lundesgaard.ci.Ci;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class RepositoryUpdatedEvent implements Event {
	public final String repositoryName;
	public final String commitId;

	public RepositoryUpdatedEvent(String repositoryName, String commitId) {
		this.repositoryName = repositoryName;
		this.commitId = commitId;
	}

	@Override
	public void process(Ci ci) {
		ci.tasks().stream().forEach(task -> task.trigger.onEvent(ci, task, this));
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("repositoryName", repositoryName)
				.append("commitId", commitId)
				.toString();
	}
}
