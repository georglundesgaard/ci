package no.lundesgaard.ci.model.job;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.event.JobCompletedEvent;
import no.lundesgaard.ci.model.task.Task;
import no.lundesgaard.ci.model.task.TaskId;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.walkFileTree;
import static java.time.Instant.now;
import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableSet;
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
	public final Set<JobId> previousJobs;

	public static Job create(Ci ci, TaskId taskId, JobId... previousJobs) {
		int jobNumber = ci.nextJobNumberFor(taskId);
		Job job = new Job(taskId, jobNumber, previousJobs);
		LOGGER.debug("Created: {}", job);
		return ci.jobs().job(job);
	}

	private Job(TaskId taskId, int jobNumber, JobId... previousJobs) {
		this.id = taskId.id + "#" + jobNumber;
		this.taskId = taskId;
		this.jobNumber = jobNumber;
		this.state = CREATED;
		this.created = now();
		this.updated = null;
		this.started = null;
		this.stopped = null;
		this.message = null;
		Set<JobId> jobIdSet = new HashSet<>();
		addAll(jobIdSet, previousJobs);
		this.previousJobs = unmodifiableSet(jobIdSet);
	}

	private Job(Job oldJob, State newState, String message) {
		this.id = oldJob.id;
		this.taskId = oldJob.taskId;
		this.jobNumber = oldJob.jobNumber;
		this.state = newState;
		this.created = oldJob.created;
		this.updated = now();
		this.message = message;
		this.previousJobs = oldJob.previousJobs;
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
		Path workspacePath = task.initWorkspace(ci, this);
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

	public void copyWorkspaceTo(Ci ci, Path targetWorkspacePath) {
		Path workspacePath = ci.workspacesPath.resolve(id);
		try {
			walkFileTree(workspacePath, copyFileVisitor(workspacePath, targetWorkspacePath));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private FileVisitor<Path> copyFileVisitor(Path source, Path target) {
		return new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				copy(file, target.resolve(source.relativize(file)));
				return CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Path targetDir = target.resolve(source.relativize(dir));
				try {
					copy(dir, targetDir);
				} catch (FileAlreadyExistsException e) {
					if (!isDirectory(targetDir))
						throw e;
				}
				return CONTINUE;
			}
		};
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
