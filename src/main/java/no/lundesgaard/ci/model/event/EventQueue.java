package no.lundesgaard.ci.model.event;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class EventQueue {
	private final Queue<Event> queue = new LinkedList<>();

	public Event next() {
		try {
			return queue.remove();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public void add(Event event) {
		queue.add(event);
	}
}
