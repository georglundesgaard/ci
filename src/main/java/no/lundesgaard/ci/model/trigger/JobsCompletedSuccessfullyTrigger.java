package no.lundesgaard.ci.model.trigger;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.event.Event;
import no.lundesgaard.ci.model.event.JobCompletedEvent;
import no.lundesgaard.ci.model.job.Job;
import no.lundesgaard.ci.model.task.Task;
import no.lundesgaard.ci.model.task.TaskId;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableSet;
import static no.lundesgaard.ci.model.task.TaskId.taskId;

public class JobsCompletedSuccessfullyTrigger implements Trigger {
	private static final Logger LOGGER = LoggerFactory.getLogger(JobsCompletedSuccessfullyTrigger.class);

	public final Set<TaskId> taskIds;

	public JobsCompletedSuccessfullyTrigger(TaskId... tasks) {
		Set<TaskId> taskSet = new HashSet<>();
		addAll(taskSet, tasks);
		this.taskIds = unmodifiableSet(taskSet);
	}

	@Override
	public void onEvent(Ci ci, Task task, Event event) {
		if (!(event instanceof JobCompletedEvent)) {
			return;
		}
		JobCompletedEvent jobCompletedEvent = (JobCompletedEvent) event;
		Job job = ci.jobs().job(jobCompletedEvent.jobId);
		if (!taskIds.contains(job.taskId)) {
			return;
		}
		if (taskIds.size() == 1) {
			LOGGER.debug("Task <{}> triggered by event: {}", task.name, event);
			Job newJob = Job.create(ci, taskId(task));
			newJob.queue(ci);
		}
		// TODO
		throw new NotImplementedException("TODO - taskIds.size() > 1");
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("taskIds", taskIds)
				.toString();
	}
}
