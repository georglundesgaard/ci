package no.lundesgaard.ci.event;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.task.Task;
import no.lundesgaard.ci.model.trigger.RepositoryUpdatedTrigger;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class RepositoryUpdatedEvent implements Event {
	private final String name;

	public RepositoryUpdatedEvent(String name) {
		this.name = name;
	}

	@Override
	public void process(Ci ci) {
		ci.tasks().stream()
				.filter(this::taskFilter)
				.map(task -> task.name)
				.forEach(ci::addTaskToQueue);
	}

	private boolean taskFilter(Task task) {
		return task.trigger instanceof RepositoryUpdatedTrigger
				&& ((RepositoryUpdatedTrigger) task.trigger).repository.equals(name);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("name", name)
				.toString();
	}
}
