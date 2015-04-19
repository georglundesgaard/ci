package no.lundesgaard.ci.command.list;

import no.lundesgaard.ci.Ci;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListTaskCommand extends ListCommand {
	private final static Logger LOGGER = LoggerFactory.getLogger(ListTaskCommand.class);
	public final static ListTaskCommand INSTANCE = new ListTaskCommand();

	private ListTaskCommand() {
	}

	@Override
	public void execute(Ci ci) {
		int count = ci.tasks().count();
		LOGGER.debug("{} {}", count, count == 1 ? "task" : "tasks");
		ci.tasks()
				.stream()
				.forEach(task -> {
					LOGGER.debug("{}", task);
					LOGGER.debug("script: \n{}", task.script);
				});
	}

	@Override
	public void validate() {
		// nothing to validate
	}
}
