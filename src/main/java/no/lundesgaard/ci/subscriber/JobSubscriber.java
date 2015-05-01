package no.lundesgaard.ci.subscriber;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.JobRunner;
import no.lundesgaard.ci.model.job.Job;
import no.lundesgaard.ci.model.job.JobId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

import static org.apache.commons.lang3.StringUtils.uncapitalize;

public class JobSubscriber extends Subscriber<JobId> implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(JobSubscriber.class);

	private final Ci ci;
	private boolean jobRunning;

	public JobSubscriber(Ci ci) {
		this.ci = ci;
	}

	@Override
	public void onCompleted() {
		LOGGER.debug("subscriber completed");
	}

	@Override
	public void onError(Throwable e) {
		LOGGER.error("subscriber error", e);
	}

	@Override
	public void onNext(JobId nextJobId) {
		Job job = ci.jobs().job(nextJobId);
		LOGGER.debug("Job accepted: {}", job);
		JobRunner jobRunner = new JobRunner(ci, job);
		new Thread(jobRunner, jobRunner.name()).start();
		this.jobRunning = true;
		LOGGER.debug("Job started");
		do {
			sleep();
		} while (jobRunner.isRunning());
		this.jobRunning = false;
		LOGGER.debug("Job terminated");
	}

	private void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// ignore exception
		}
	}

	public boolean isActive() {
		return !isUnsubscribed() || jobRunning;
	}

	@Override
	public void run() {
		ci.jobQueue().subscribe(this);
	}

	public void start() {
		String name = uncapitalize(getClass().getSimpleName());
		new Thread(this, name).start();
		LOGGER.debug("Job subscriber started");
	}

	public void stop() {
		unsubscribe();
		LOGGER.debug("Job subscriber stopping...");
		while (isActive()) {
			sleep();
		}
		LOGGER.debug("Job subscriber stopped");
	}
}
