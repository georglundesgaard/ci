package no.lundesgaard.ci.model.command.list;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.job.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

import static no.lundesgaard.ci.model.task.TaskId.taskId;

public class ListJobsCommand extends ListCommand {
	private final static Logger LOGGER = LoggerFactory.getLogger(ListJobsCommand.class);
	private final String task;

	public ListJobsCommand(Properties commandProperties) {
		this.task = commandProperties.getProperty("task");
	}

	@Override
	public void execute(Ci ci) {
		List<Job> jobs = ci.jobs().forTask(taskId(task));
		int count = jobs.size();
		LOGGER.debug("{} {} for task <{}>", count, count == 1 ? "job" : "jobs", task);
		jobs.stream()
				.map(Job::toString)
				.forEach(LOGGER::debug);
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
