package no.lundesgaard.ci.model.event;

import no.lundesgaard.ci.model.ObservableQueue;

import java.util.LinkedList;

public class EventQueue extends ObservableQueue<Event> {
	public EventQueue() {
		super(new LinkedList<>());
	}
}
