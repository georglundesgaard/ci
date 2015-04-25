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
				int repositoriesScanned = ci.repositories().scan(ci);
				if (repositoriesScanned > 0) {
					LOGGER.debug("Repositories scanned: {}", repositoriesScanned);
				}
				sleep();
			}
		} finally {
			state = STOPPED;
			LOGGER.debug("Repository processor stopped");
		}
	}

	private void init() {
		if (state != CREATED) {
			throw new IllegalStateException("Repository processor is already running");
		}
		LOGGER.debug("Repository processor started");
		state = RUNNING;
	}
}
