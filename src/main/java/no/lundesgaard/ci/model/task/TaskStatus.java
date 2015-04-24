package no.lundesgaard.ci.model.task;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class TaskStatus implements Serializable {
	public final String taskName;
	public final String taskRunnerId;
	public final Instant timestamp = Instant.now();
	public final State state;
	public final String message;
	public final Exception exception;
	private final Map<String, String> properties = new HashMap<>();

	public TaskStatus(String taskName, String taskRunnerId, State state, String message, Exception exception) {
		this.taskName = taskName;
		this.taskRunnerId = taskRunnerId;
		this.state = state;
		this.message = message;
		this.exception = exception;
	}

	public TaskStatus(TaskStatus taskStatus, State state, String message, Exception exception) {
		this.taskName = taskStatus.taskName;
		this.taskRunnerId = taskStatus.taskRunnerId;
		this.state = state;
		this.message = message;
		this.exception = exception;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		TaskStatus that = (TaskStatus) o;

		return new EqualsBuilder()
				.append(taskName, that.taskName)
				.append(taskRunnerId, that.taskRunnerId)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(taskName)
				.append(taskRunnerId)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("taskName", taskName)
				.append("taskRunnerId", taskRunnerId)
				.append("timestamp", timestamp)
				.append("state", state)
				.append("message", message)
				.toString();
	}

	public enum State {
		RUNNING, STOPPED, ERROR, COMPLETED
	}
}
