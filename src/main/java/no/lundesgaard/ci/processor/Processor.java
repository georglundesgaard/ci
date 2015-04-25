package no.lundesgaard.ci.processor;

import no.lundesgaard.ci.Ci;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.lundesgaard.ci.processor.Processor.State.CREATED;
import static no.lundesgaard.ci.processor.Processor.State.STOPPED;

public abstract class Processor implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);

	protected final Ci ci;
	public State state;

	public Processor(Ci ci) {
		this.ci = ci;
		this.state = CREATED;
	}

	protected void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			LOGGER.debug("{} sleep interrupted", this, e);
		}
	}

	public void stop() {
		state = State.STOPPING;
	}

	public boolean isStopped() {
		return state == STOPPED;
	}

	public boolean isStarted() {
		return state != CREATED;
	}

	public enum State {
		CREATED, WAITING, RUNNING, STOPPING, STOPPED
	}
}
