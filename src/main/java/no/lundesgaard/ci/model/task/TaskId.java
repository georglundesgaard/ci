package no.lundesgaard.ci.model.task;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public final class TaskId implements Serializable {
	public final String id;

	public static TaskId taskId(String id) {
		return new TaskId(id);
	}

	public static TaskId taskId(Task task) {
		return taskId(task.name);
	}

	private TaskId(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		TaskId taskId = (TaskId) o;

		return new EqualsBuilder()
				.append(id, taskId.id)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(id)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", id)
				.toString();
	}
}