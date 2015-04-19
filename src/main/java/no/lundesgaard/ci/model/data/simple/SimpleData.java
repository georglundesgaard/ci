package no.lundesgaard.ci.model.data.simple;

import no.lundesgaard.ci.model.data.Data;
import no.lundesgaard.ci.model.repository.Repositories;
import no.lundesgaard.ci.model.task.TaskQueue;
import no.lundesgaard.ci.model.task.TaskStatus;
import no.lundesgaard.ci.model.task.TaskStatuses;
import no.lundesgaard.ci.model.task.TaskStatusesMap;
import no.lundesgaard.ci.model.task.Tasks;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableCollection;

public class SimpleData implements Data {
	private final Repositories repositories = new Repositories(new HashMap<>());
	private final Tasks tasks = new Tasks(new HashMap<>());
	private final TaskQueue taskQueue = new TaskQueue(new LinkedList<>());
	private final TaskStatuses taskStatuses = new TaskStatuses(taskStatusesMap());

	private static TaskStatusesMap taskStatusesMap() {
		return new SimpleTaskStatusesMap();
	}

	@Override
	public Repositories repositories() {
		return repositories;
	}

	@Override
	public Tasks tasks() {
		return tasks;
	}

	@Override
	public TaskQueue taskQueue() {
		return taskQueue;
	}

	@Override
	public TaskStatuses taskStatuses() {
		return taskStatuses;
	}

	@Override
	public void shutdown() {
		// do nothing
	}

	private static class SimpleTaskStatusesMap implements TaskStatusesMap {
		private Map<String, Set<TaskStatus>> taskStatusesMap = new HashMap<>();

		@Override
		public void put(String taskName, TaskStatus taskStatus) {
			Set<TaskStatus> taskStatuses = taskStatusesMap.get(taskName);
			if (taskStatuses == null) {
				taskStatuses = new HashSet<>();
				taskStatusesMap.put(taskName, taskStatuses);
			}
			if (taskStatuses.contains(taskStatus)) {
				taskStatuses.remove(taskStatus);
			}
			taskStatuses.add(taskStatus);
		}

		@Override
		public Collection<TaskStatus> get(String taskName) {
			if (taskStatusesMap.containsKey(taskName)) {
				return unmodifiableCollection(taskStatusesMap.get(taskName));
			}
			return emptySet();
		}
	}
}
