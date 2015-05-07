package no.lundesgaard.ci;

import no.lundesgaard.ci.model.job.Job;
import no.lundesgaard.ci.model.job.JobId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscription;

public class JobRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(JobRunner.class);

	private final Ci ci;
	private JobId jobId;
	private Process process;
	private Subscription subscription;

	public JobRunner(Ci ci) {
		this.ci = ci;
	}

	public void startSubscription() {
		if (subscription != null && !subscription.isUnsubscribed()) {
			throw new IllegalStateException("Job queue subscription already started");
		}
		this.subscription = ci.jobQueue().subscribe(this::runJob);
	}

	public void stopSubscription() {
		if (subscription != null && !subscription.isUnsubscribed()) {
			subscription.unsubscribe();
		}
		this.subscription = null;
	}

	public boolean isRunning() {
		return process != null && process.isAlive();
	}

	private void runJob(JobId jobId) {
		this.jobId = jobId;
		this.process = startJob();
		waitForProcessToFinish();
	}

	private Process startJob() {
		LOGGER.debug("Starting job...");
		Job job = ci.jobs().job(jobId);
		Process process = job.start(ci);
		LOGGER.debug("Job started");
		return process;
	}

	private void waitForProcessToFinish() {
		LOGGER.debug("Waiting for process to terminate");
		try {
			int exitCode = process.waitFor();
			Job job = ci.jobs().job(jobId);
			if (exitCode == 0) {
				job.complete(ci);
			} else {
				job.fail(ci, exitCode);
			}
			LOGGER.debug("Process terminated. Exit code: {}", exitCode);
		} catch (InterruptedException e) {
			LOGGER.warn("Wait for process termination was interrupted.", e);
		} finally {
			this.process = null;
			this.jobId = null;
		}
	}
}
