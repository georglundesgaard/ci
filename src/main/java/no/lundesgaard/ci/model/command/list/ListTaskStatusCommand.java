package no.lundesgaard.ci.model.command.list;

import no.lundesgaard.ci.Ci;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

// TODO
public class ListTaskStatusCommand extends ListCommand {
	private final static Logger LOGGER = LoggerFactory.getLogger(ListTaskStatusCommand.class);
	private final String task;

	public ListTaskStatusCommand(Properties commandProperties) {
		this.task = commandProperties.getProperty("task");
	}

	@Override
	public void execute(Ci ci) {
//		Collection<TaskStatus> taskStatuses = ci.taskStatuses().forTask(task);
//		int count = taskStatuses.size();
//		LOGGER.debug("{} {} for task <{}>", count, count == 1 ? "status" : "statuses", task);
//		taskStatuses
//				.stream()
//				.map(TaskStatus::toString)
//				.forEach(LOGGER::debug);
	}

	@Override
	public void validate() {
		validateTask();
	}

	private void validateTask() {
		if (task == null) {
			throw new IllegalStateException("missing task");
		}
	}
}
