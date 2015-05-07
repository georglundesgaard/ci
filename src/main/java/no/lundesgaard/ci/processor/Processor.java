package no.lundesgaard.ci.processor;

import no.lundesgaard.ci.Ci;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscription;

import static no.lundesgaard.ci.processor.Processor.State.CREATED;
import static no.lundesgaard.ci.processor.Processor.State.RUNNING;
import static no.lundesgaard.ci.processor.Processor.State.STOPPED;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

public abstract class Processor implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);

	protected final Ci ci;
	public State state;
	private Subscription subscription;

	public Processor(Ci ci) {
		this.ci = ci;
		this.state = CREATED;
	}

	protected void sleep() {
		try {
			Thread.sleep(100);
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

	public boolean isRunning() {
		return state == RUNNING;
	}

	public enum State {
		CREATED, RUNNING, STOPPING, STOPPED
	}
}
