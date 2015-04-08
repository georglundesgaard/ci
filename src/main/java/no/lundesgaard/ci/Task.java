package no.lundesgaard.ci;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Properties;

public abstract class Task implements Serializable {
	private final Trigger trigger;
	private boolean running;
	private Instant lastExecuted;

	public Task(Properties taskProperties) {
		this.trigger = Trigger.valueOf(taskProperties.getProperty("trigger"));
	}

	public static Task from(Path taskPath) throws IOException {
		String taskId = taskPath.getFileName().toString();
		Type type = Type.from(taskId);
		switch (type) {
			case BUILD:
				return BuildTask.from(taskPath);
			default:
				throw new UnsupportedOperationException("Task type <" + type + "> not implemented");
		}
	}

	public boolean isReady() {
		return !running;
	}

	public boolean isTriggerExpired() {
		return trigger.isExpired(lastExecuted);
	}

	public void execute(Ci ci, String taskId) {
		if (running) {
			throw new IllegalStateException("task is already running");
		}
		running = true;
	}

	public void stop() {
		running = false;
		lastExecuted = Instant.now();
	}

	public void stop(Properties taskProperties) {
		stop();
	}

	public enum Type {
		BUILD;

		public static Type from(String taskId) {
			String taskType = taskId.substring(0, taskId.indexOf('-')).toUpperCase();
			try {
				return valueOf(taskType);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Unknown task type <" + taskType + ">");
			}
		}
	}
}
