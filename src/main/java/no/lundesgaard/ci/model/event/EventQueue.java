package no.lundesgaard.ci.model.event;

import no.lundesgaard.ci.model.ObservableQueue;

import java.util.concurrent.LinkedBlockingQueue;

public class EventQueue extends ObservableQueue<Event> {
	public EventQueue() {
		super(new LinkedBlockingQueue<>());
	}
}
