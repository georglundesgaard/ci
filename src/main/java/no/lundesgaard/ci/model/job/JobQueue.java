package no.lundesgaard.ci.model.job;

import no.lundesgaard.ci.model.ObservableQueue;

import java.util.Queue;

public class JobQueue extends ObservableQueue<JobId> {
	public JobQueue(Queue<JobId> queue) {
		super(queue);
	}
}
