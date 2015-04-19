package no.lundesgaard.ci.model;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.task.Task;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.lang.ProcessBuilder.Redirect.appendTo;
import static java.lang.String.format;
import static no.lundesgaard.ci.model.task.TaskStatus.State.COMPLETED;
import static no.lundesgaard.ci.model.task.TaskStatus.State.ERROR;
import static no.lundesgaard.ci.model.task.TaskStatus.State.RUNNING;
import static no.lundesgaard.ci.model.task.TaskStatus.State.STOPPED;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class TaskRunner implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskRunner.class);
	public static final String OUTPUT_LOG = "output.log";

	public final String id;
	public final String scriptName;
	private final Ci ci;
	public final Task task;
	private Process process;
	private Instant started;

	public TaskRunner(Ci ci, Task task) {
		this.id = task.name + "-" + UUID.randomUUID().toString();
		this.scriptName = id + ".sh";
		this.ci = ci;
		this.task = task;
	}

	@Override
	public void run() {
		try {
			Path workspacePath = initWorkspace();
			writeScriptFile(workspacePath);
			File outputLog = workspacePath.resolve(OUTPUT_LOG).toFile();
			process = new ProcessBuilder("sh", scriptName)
					.directory(workspacePath.toFile())
					.redirectOutput(appendTo(outputLog))
					.redirectError(appendTo(outputLog))
					.start();
			ci.addTaskStatus(this, RUNNING, null, null);
			started = Instant.now();
			LOGGER.debug("{} started...", task);
		} catch (IOException e) {
			ci.addTaskStatus(this, ERROR, "Failed to start task process", e);
			LOGGER.error("Failed to start task process", e);
		}
		try {
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				String message = format("Duration: %s", Duration.between(started, Instant.now()));
				ci.updateTaskStatus(this, COMPLETED, message, null);
			} else {
				String message = format("Process failed. Exit code: %d", exitCode);
				ci.updateTaskStatus(this, ERROR, message, null);
			}
			LOGGER.debug("{} completed. Exit code: {}", this, exitCode);
		} catch (InterruptedException e) {
			LOGGER.warn("Waiting for process <{}> was interrupted.", process, e);
		}
	}

	private Path initWorkspace() throws IOException {
		Path workspacesPath = ci.getWorkspacesPath();
		Path workspacePath = workspacesPath.resolve(id);
		return task.workspace.init(ci, workspacePath);
	}

	private void writeScriptFile(Path workspacePath) throws IOException {
		Path scriptPath = workspacePath.resolve(scriptName);
		try (FileWriter fileWriter = new FileWriter(scriptPath.toFile())) {
			fileWriter.write(task.script);
		}
	}

	public boolean isRunning() {
		return process != null && process.isAlive();
	}

	public void stop() {
		if (process == null) {
			return;
		}
		process.destroy();
		new Thread(() -> {
			try {
				boolean exited = process.waitFor(1, TimeUnit.MINUTES);
				int exitCode;
				if (!exited) {
					LOGGER.debug("Timed out waiting for process to stop: {}", process);
					Process process = this.process.destroyForcibly();
					exitCode = process.waitFor();
				} else {
					exitCode = process.exitValue();
				}
				String message = format("%s stopped. Exit code: %d", this, exitCode);
				ci.updateTaskStatus(this, STOPPED, message, null);
				LOGGER.debug("{} stopped. Exit code: {}", this, exitCode);
			} catch (InterruptedException e) {
				LOGGER.warn("Waiting for process <{}> to stop was interrupted.", process, e);
			}
		}).start();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("id", id)
				.toString();
	}
}
