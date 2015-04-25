package no.lundesgaard.ci.model.task;

import java.util.Map;
import java.util.stream.Stream;

public class Tasks {
	private final Map<String, Task> taskMap;

	public Tasks(Map<String, Task> taskMap) {
		this.taskMap = taskMap;
	}

	public Task task(TaskId taskId) {
		return taskMap.get(taskId.id);
	}

	public Task task(Task task) {
		taskMap.put(task.name, task);
		return task;
	}

	public int count() {
		return taskMap.size();
	}

	public Stream<Task> stream() {
		return taskMap.values().stream();
	}
}
