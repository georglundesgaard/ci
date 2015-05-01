package no.lundesgaard.ci.model.job;

import rx.Observable;
import rx.Subscriber;

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

	public void subscribe(Subscriber<JobId> subscriber) {
		jobIdObservable().subscribe(subscriber);
	}

	private Observable<JobId> jobIdObservable() {
		return Observable.create(subscriber -> {
			while (!subscriber.isUnsubscribed()) {
				JobId nextJobId = next();
				if (nextJobId != null) {
					subscriber.onNext(nextJobId);
				} else {
					sleep();
				}
			}
		});
	}

	private void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// ignore exception
		}
	}
}
