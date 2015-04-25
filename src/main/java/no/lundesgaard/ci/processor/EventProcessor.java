package no.lundesgaard.ci.processor;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.lundesgaard.ci.processor.Processor.State.CREATED;
import static no.lundesgaard.ci.processor.Processor.State.RUNNING;
import static no.lundesgaard.ci.processor.Processor.State.STOPPED;

public class EventProcessor extends Processor {
	private static final Logger LOGGER = LoggerFactory.getLogger(EventProcessor.class);

	public EventProcessor(Ci ci) {
		super(ci);
	}

	@Override
	public void run() {
		init();
		try {
			while (state == RUNNING) {
				processEvents();
				sleep();
			}
		} finally {
			state = STOPPED;
			LOGGER.debug("Event processor stopped");
		}
	}

	private void init() {
		if (state != CREATED) {
			throw new IllegalStateException("Event processor is already running");
		}
		LOGGER.debug("Event processor started");
		state = RUNNING;
	}

	private void processEvents() {
		Event event;
		while ((event = ci.eventQueue.next()) != null) {
			LOGGER.debug("Event accepted: {}", event);
			event.process(ci);
			LOGGER.debug("Event processed");
		}
	}
}
