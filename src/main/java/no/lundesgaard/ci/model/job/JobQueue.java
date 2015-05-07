package no.lundesgaard.ci.model.job;

import no.lundesgaard.ci.model.ObservableQueue;

import java.util.concurrent.BlockingQueue;

public class JobQueue extends ObservableQueue<JobId> {
	public JobQueue(BlockingQueue<JobId> queue) {
		super(queue);
	}
}
