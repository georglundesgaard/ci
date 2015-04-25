package no.lundesgaard.ci.model.job;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.event.JobCompletedEvent;
import no.lundesgaard.ci.model.task.Task;
import no.lundesgaard.ci.model.task.TaskId;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.file.Path;
import java.time.Instant;

import static java.lang.String.format;
import static java.time.Instant.now;
import static no.lundesgaard.ci.model.job.Job.State.COMPLETED;
import static no.lundesgaard.ci.model.job.Job.State.CREATED;
import static no.lundesgaard.ci.model.job.Job.State.FAILED;
import static no.lundesgaard.ci.model.job.Job.State.RUNNING;
import static no.lundesgaard.ci.model.job.Job.State.WAITING;
import static no.lundesgaard.ci.model.job.JobId.jobId;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class Job implements Serializable {
	private final static Logger LOGGER = LoggerFactory.getLogger(Job.class);

	public final String id;
	public final TaskId taskId;
	public final int jobNumber;
	public final State state;
	public final Instant created;
	public final Instant updated;
	public final Instant started;
	public final Instant stopped;
	public final String message;

	public static Job create(Ci ci, TaskId taskId) {
		int jobNumber = ci.nextJobNumberFor(taskId);
		Job job = new Job(taskId, jobNumber);
		LOGGER.debug("Created: {}", job);
		return ci.jobs().job(job);
	}

	private Job(TaskId taskId, int jobNumber) {
		this.id = taskId.id + "#" + jobNumber;
		this.taskId = taskId;
		this.jobNumber = jobNumber;
		this.state = CREATED;
		this.created = now();
		this.updated = null;
		this.started = null;
		this.stopped = null;
		this.message = null;
	}

	private Job(Job oldJob, State newState, String message) {
		this.id = oldJob.id;
		this.taskId = oldJob.taskId;
		this.jobNumber = oldJob.jobNumber;
		this.state = newState;
		this.created = oldJob.created;
		this.updated = now();
		this.message = message;
		switch (newState) {
			case RUNNING:
				this.started = now();
				this.stopped = null;
				break;
			case COMPLETED:
			case FAILED:
				this.started = oldJob.started;
				this.stopped = now();
				break;
			case WAITING:
				this.started = null;
				this.stopped = null;
				break;
			default:
				throw new IllegalStateException(format("unexpected new state: %s", state));
		}
	}

	public Job queue(Ci ci) {
		verifyState(CREATED);
		Job job = updateJob(ci, WAITING, null);
		ci.jobQueue().add(jobId(job));
		LOGGER.debug("Queued: {}", job);
		return job;
	}

	public Process start(Ci ci) {
		verifyState(WAITING);
		Task task = ci.tasks().task(taskId);
		Path workspacePath = task.initWorkspace(ci, id);
		Process process = task.startProcess(workspacePath);
		Job job = updateJob(ci, RUNNING, null);
		LOGGER.debug("Started: {}", job);
		return process;
	}

	public Job complete(Ci ci) {
		verifyState(RUNNING);
		Job job = updateJob(ci, COMPLETED, null);
		ci.eventQueue.add(new JobCompletedEvent(jobId(job)));
		LOGGER.debug("Completed: {}", job);
		return job;
	}

	public Job fail(Ci ci, int exitCode) {
		verifyState(RUNNING);
		Job job = updateJob(ci, FAILED, format("Exit code: %d", exitCode));
		LOGGER.debug("Failed: {}", job);
		return job;
	}

	private void verifyState(State state) {
		if (this.state != state) {
			throw new IllegalStateException(format("expected state <%s>, but got <%s>", state, this.state));
		}
	}

	private Job updateJob(Ci ci, State newState, String message) {
		return ci.jobs().job(new Job(this, newState, message));
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("id", id)
				.append("state", state)
				.append("created", created)
				.append("updated", updated)
				.append("started", started)
				.append("stopped", stopped)
				.append("message", message)
				.toString();
	}

	public enum State {
		CREATED, WAITING, RUNNING, COMPLETED, FAILED
	}
}
