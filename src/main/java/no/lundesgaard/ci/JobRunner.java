package no.lundesgaard.ci;

import no.lundesgaard.ci.model.job.Job;
import no.lundesgaard.ci.model.job.JobId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		startProcess();
		waitForProcessToFinish();
	}

	private void startProcess() {
		process = job.start(ci);
		job = ci.jobs().job(new JobId(job.id));
		LOGGER.debug("Process started: {}", process);
	}

	private void waitForProcessToFinish() {
		LOGGER.debug("Waiting for process to finish");
		try {
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				job = job.complete(ci);
			} else {
				job = job.fail(ci, exitCode);
			}
			LOGGER.debug("Finished. Exit code: {}", exitCode);
		} catch (InterruptedException e) {
			LOGGER.warn("Waiting for process <{}> was interrupted.", process, e);
		}
	}

	public boolean isRunning() {
		return process != null && process.isAlive();
	}
}
