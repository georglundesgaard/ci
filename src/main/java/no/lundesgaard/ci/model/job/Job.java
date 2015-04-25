package no.lundesgaard.ci.model.job;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Instant;

import static java.lang.String.format;
import static java.time.Instant.now;
import static no.lundesgaard.ci.model.job.Job.State.COMPLETED;
import static no.lundesgaard.ci.model.job.Job.State.CREATED;
import static no.lundesgaard.ci.model.job.Job.State.FAILED;
import static no.lundesgaard.ci.model.job.Job.State.RUNNING;
import static no.lundesgaard.ci.model.job.Job.State.WAITING;

public class Job {
	private final static Logger LOGGER = LoggerFactory.getLogger(Job.class);

	public final String id;
	public final String taskName;
	public final int jobNumber;
	public final State state;
	public final Instant created;
	public final Instant updated;
	public final Instant started;
	public final Instant stopped;
	public final String message;

	public static Job create(Ci ci, String taskName) {
		int jobNumber = ci.nextJobNumberFor(taskName);
		Job job = new Job(taskName, jobNumber);
		LOGGER.debug("Created: {}", job);
		return ci.jobs().job(job);
	}

	private Job(String taskName, int jobNumber) {
		this.id = taskName + "#" + jobNumber;
		this.taskName = taskName;
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
		this.taskName = oldJob.taskName;
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
		ci.jobQueue().add(new JobId(job.id));
		LOGGER.debug("Queued: {}", job);
		return job;
	}

	public Process start(Ci ci) {
		verifyState(WAITING);
		Task task = ci.tasks().task(taskName);
		Path workspacePath = task.initWorkspace(ci, id);
		Process process = task.startProcess(workspacePath);
		Job job = updateJob(ci, RUNNING, null);
		LOGGER.debug("Started: {}", job);
		return process;
	}

	public Job complete(Ci ci) {
		verifyState(RUNNING);
		Job job = updateJob(ci, COMPLETED, null);
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

	public enum State {
		CREATED, WAITING, RUNNING, COMPLETED, FAILED
	}
}
