package no.lundesgaard.ci.processor;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscription;

public class EventProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(EventProcessor.class);

	private final Ci ci;
	private Subscription subscription;

	public EventProcessor(Ci ci) {
		this.ci = ci;
	}

	public void startSubscription() {
		if (subscription != null && !subscription.isUnsubscribed()) {
			throw new IllegalStateException("Event queue subscription already started");
		}
		this.subscription = ci.eventQueue.subscribe(this::processEvent);
	}

	private void processEvent(Event event) {
		LOGGER.debug("Event accepted: {}", event);
		event.process(ci);
		LOGGER.debug("Event processed");
	}

	public void stopSubscription() {
		if (subscription != null && !subscription.isUnsubscribed()) {
			subscription.unsubscribe();
		}
		this.subscription = null;
	}
}
