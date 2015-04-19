package no.lundesgaard.ci.model.task;

import no.lundesgaard.ci.model.TaskRunner;

import java.util.Collection;

public class TaskStatuses {
	private final TaskStatusesMap taskStatusesMap;

	public TaskStatuses(TaskStatusesMap taskStatusesMap) {
		this.taskStatusesMap = taskStatusesMap;
	}

	public TaskStatus taskStatus(TaskStatus taskStatus) {
		taskStatusesMap.put(taskStatus.taskName, taskStatus);
		return taskStatus;
	}

	public TaskStatus taskStatus(TaskRunner taskRunner) {
		return forTask(taskRunner.task.name)
				.stream()
				.filter(taskStatus -> taskStatus.taskRunnerId.equals(taskRunner.id))
				.findFirst()
				.orElse(null);
	}

	public Collection<TaskStatus> forTask(String taskName) {
		return taskStatusesMap.get(taskName);
	}
}
