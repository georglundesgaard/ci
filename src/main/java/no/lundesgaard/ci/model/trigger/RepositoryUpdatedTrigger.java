package no.lundesgaard.ci.model.trigger;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.event.Event;
import no.lundesgaard.ci.model.event.RepositoryUpdatedEvent;
import no.lundesgaard.ci.model.job.Job;
import no.lundesgaard.ci.model.task.Task;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class RepositoryUpdatedTrigger implements Trigger {
	public final String repositoryName;

	public RepositoryUpdatedTrigger(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	@Override
	public void onEvent(Ci ci, Task task, Event event) {
		if (!(event instanceof RepositoryUpdatedEvent)) {
			return;
		}
		RepositoryUpdatedEvent repositoryUpdatedEvent = (RepositoryUpdatedEvent) event;
		if (!this.repositoryName.equals(repositoryUpdatedEvent.repositoryName)) {
			return;
		}
		ci.jobQueue().add(new Job(task.name, ci.nextJobNumberFor(task.name)));
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("repositoryName", repositoryName)
				.toString();
	}
}
