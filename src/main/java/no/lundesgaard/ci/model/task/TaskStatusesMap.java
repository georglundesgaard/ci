package no.lundesgaard.ci.model.task;

import java.util.Collection;

public interface TaskStatusesMap {
	void put(String taskName, TaskStatus taskStatus);
	Collection<TaskStatus> get(String taskName);
}
