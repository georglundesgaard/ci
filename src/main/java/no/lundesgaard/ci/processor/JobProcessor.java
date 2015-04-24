package no.lundesgaard.ci.processor;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.JobRunner;
import no.lundesgaard.ci.model.job.Job;
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
			LOGGER.debug("Job processor stopped");
			state = STOPPED;
		}
	}

	private void init() {
		if (state != CREATED) {
			throw new IllegalStateException("Job processor already running");
		}
		state = WAITING;
		LOGGER.debug("Job processor started");
	}

	private void tryNextJob() {
		if (state == RUNNING) {
			return;
		}
		Job job = ci.jobQueue().next();
		if (job == null) {
			return;
		}
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
