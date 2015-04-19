package no.lundesgaard.ci.model.data.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;
import no.lundesgaard.ci.model.data.Data;
import no.lundesgaard.ci.model.repository.Repositories;
import no.lundesgaard.ci.model.task.TaskQueue;
import no.lundesgaard.ci.model.task.TaskStatus;
import no.lundesgaard.ci.model.task.TaskStatuses;
import no.lundesgaard.ci.model.task.TaskStatusesMap;
import no.lundesgaard.ci.model.task.Tasks;

import java.util.Collection;

public class HazelcastData implements Data {
	private final HazelcastInstance hazelcastInstance;

	public HazelcastData(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	@Override
	public Repositories repositories() {
		return new Repositories(hazelcastInstance.getMap("repositoryMap"));
	}

	@Override
	public Tasks tasks() {
		return new Tasks(hazelcastInstance.getMap("taskMap"));
	}

	@Override
	public TaskQueue taskQueue() {
		return new TaskQueue(hazelcastInstance.getQueue("taskQueue"));
	}

	@Override
	public TaskStatuses taskStatuses() {
		return new TaskStatuses(new HazelcastTaskStatusesMap(hazelcastInstance));
	}

	@Override
	public void shutdown() {
		hazelcastInstance.shutdown();
	}

	private static class HazelcastTaskStatusesMap implements TaskStatusesMap {
		private MultiMap<String, TaskStatus> taskStatusesMap;

		public HazelcastTaskStatusesMap(HazelcastInstance hazelcastInstance) {
			this.taskStatusesMap = hazelcastInstance.getMultiMap("taskStatusesMap");
		}

		@Override
		public void put(String taskName, TaskStatus taskStatus) {
			if (taskStatusesMap.containsEntry(taskName, taskStatus)) {
				taskStatusesMap.remove(taskName, taskStatus);
			}
			taskStatusesMap.put(taskName, taskStatus);
		}

		@Override
		public Collection<TaskStatus> get(String taskName) {
			return taskStatusesMap.get(taskName);
		}
	}
}
