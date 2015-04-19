package no.lundesgaard.ci.model.task;

import java.util.NoSuchElementException;
import java.util.Queue;

public class TaskQueue {
	private final Queue<String> queue;

	public TaskQueue(Queue<String> queue) {
		this.queue = queue;
	}

	public void add(String name) {
		this.queue.add(name);
	}

	public String next() {
		try {
			return this.queue.remove();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
}
