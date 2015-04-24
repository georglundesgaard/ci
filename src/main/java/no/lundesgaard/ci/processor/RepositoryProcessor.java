package no.lundesgaard.ci.processor;

import no.lundesgaard.ci.Ci;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.lundesgaard.ci.processor.Processor.State.CREATED;
import static no.lundesgaard.ci.processor.Processor.State.RUNNING;
import static no.lundesgaard.ci.processor.Processor.State.STOPPED;

public class RepositoryProcessor extends Processor {
	private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryProcessor.class);

	public RepositoryProcessor(Ci ci) {
		super(ci);
	}

	@Override
	public void run() {
		init();
		try {
			while (state == RUNNING) {
				ci.repositories().scan(ci);
				sleep();
			}
		} finally {
			LOGGER.debug("Repository processor stopped");
			state = STOPPED;
		}
	}

	private void init() {
		if (state != CREATED) {
			throw new IllegalStateException("Repository processor already running");
		}
		state = RUNNING;
		LOGGER.debug("Repository processor started");
	}
}
