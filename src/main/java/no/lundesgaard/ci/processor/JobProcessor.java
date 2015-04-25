package no.lundesgaard.ci.processor;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.JobRunner;
import no.lundesgaard.ci.model.job.Job;
import no.lundesgaard.ci.model.job.JobId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.lundesgaard.ci.processor.Processor.State.CREATED;
import static no.lundesgaard.ci.processor.Processor.State.RUNNING;
import static no.lundesgaard.ci.processor.Processor.State.STOPPED;
import static no.lundesgaard.ci.processor.Processor.State.WAITING;

public class JobProcessor extends Processor {
	private static final Logger LOGGER = LoggerFactory.getLogger(JobProcessor.class);

	private JobRunner currentJobRunner;

	public JobProcessor(Ci ci) {
		super(ci);
	}

	@Override
	public void run() {
		init();
		try {
			while (state == WAITING | state == RUNNING) {
				tryNextJob();
				sleep();
			}
		} finally {
			LOGGER.debug("Job processor stopping...");
			stopJobRunner();
			state = STOPPED;
			LOGGER.debug("Job processor stopped");
		}
	}

	private void init() {
		if (state != CREATED) {
			throw new IllegalStateException("Job processor is already running");
		}
		LOGGER.debug("Job processor started");
		state = WAITING;
	}

	private void tryNextJob() {
		if (state == RUNNING) {
			return;
		}
		JobId nextJobId = ci.jobQueue().next();
		if (nextJobId == null) {
			return;
		}
		Job job = ci.jobs().job(nextJobId);
		LOGGER.debug("Job accepted: {}", job);
		currentJobRunner = new JobRunner(ci, job);
		new Thread(currentJobRunner, currentJobRunner.name()).start();
	}

	private void stopJobRunner() {
		if (currentJobRunner == null || !currentJobRunner.isRunning()) {
			return;
		}
		LOGGER.debug("Waiting for current job to finish");
		do {
			sleep();
		} while (currentJobRunner.isRunning());
	}
}
