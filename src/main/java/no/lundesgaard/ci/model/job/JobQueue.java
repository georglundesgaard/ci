package no.lundesgaard.ci.model.job;

import java.util.NoSuchElementException;
import java.util.Queue;

public class JobQueue {
	private final Queue<JobId> queue;

	public JobQueue(Queue<JobId> queue) {
		this.queue = queue;
	}

	public void add(JobId jobId) {
		this.queue.add(jobId);
	}

	public JobId next() {
		try {
			return this.queue.remove();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
}
