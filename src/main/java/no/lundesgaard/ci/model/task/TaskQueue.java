package no.lundesgaard.ci.model.task;

import no.lundesgaard.ci.task.TaskQueueItem;

import java.util.NoSuchElementException;
import java.util.Queue;

public class TaskQueue {
	private final Queue<TaskQueueItem> queue;

	public TaskQueue(Queue<TaskQueueItem> queue) {
		this.queue = queue;
	}

	public void add(TaskQueueItem item) {
		this.queue.add(item);
	}

	public TaskQueueItem next() {
		try {
			return this.queue.remove();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
}
