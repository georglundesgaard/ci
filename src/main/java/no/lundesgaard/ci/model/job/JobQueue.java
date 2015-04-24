package no.lundesgaard.ci.model.job;

import java.util.NoSuchElementException;
import java.util.Queue;

public class JobQueue {
	private final Queue<Job> queue;

	public JobQueue(Queue<Job> queue) {
		this.queue = queue;
	}

	public void add(Job job) {
		this.queue.add(job.queue());
	}

	public Job next() {
		try {
			return this.queue.remove();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
}
