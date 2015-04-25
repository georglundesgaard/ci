package no.lundesgaard.ci;

import no.lundesgaard.ci.model.job.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.lundesgaard.ci.model.job.JobId.jobId;

public class JobRunner implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(JobRunner.class);

	private final Ci ci;
	private Job job;
	private Process process;

	public JobRunner(Ci ci, Job job) {
		this.ci = ci;
		this.job = job;
	}

	public String name() {
		return "jobRunner-" + job.id;
	}

	@Override
	public void run() {
		if (process != null) {
			throw new IllegalStateException("Job runner is already started");
		}
		startJob();
		waitForProcessToFinish();
	}

	private void startJob() {
		LOGGER.debug("Starting job...");
		process = job.start(ci);
		job = ci.jobs().job(jobId(job));
		LOGGER.debug("Job started");
	}

	private void waitForProcessToFinish() {
		LOGGER.debug("Waiting for process to terminate");
		try {
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				job = job.complete(ci);
			} else {
				job = job.fail(ci, exitCode);
			}
			LOGGER.debug("Process terminated. Exit code: {}", exitCode);
		} catch (InterruptedException e) {
			LOGGER.warn("Wait for process termination was interrupted.", e);
		}
	}

	public boolean isRunning() {
		return process != null && process.isAlive();
	}
}
