package no.lundesgaard.ci.model.job;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.task.Task;

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
	public final String id;
	public final String taskName;
	public final int jobNumber;
	public final State state;
	public final Instant created;
	public final Instant updated;
	public final Instant started;
	public final Instant stopped;
	public final String message;

	public Job(String taskName, int jobNumber) {
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

	public Job queue() {
		verifyState(CREATED);
		return new Job(this, WAITING, null);
	}

	public Process start(Ci ci) {
		verifyState(WAITING);
		Task task = ci.tasks().task(taskName);
		Path workspacePath = task.initWorkspace(ci, id);
		Process process = task.startProcess(workspacePath);
		updateJob(ci, RUNNING, null);
		return process;
	}

	public Job complete(Ci ci) {
		verifyState(RUNNING);
		return updateJob(ci, COMPLETED, null);
	}

	public Job fail(Ci ci, int exitCode) {
		verifyState(RUNNING);
		return updateJob(ci, FAILED, format("Exit code: %d", exitCode));
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
