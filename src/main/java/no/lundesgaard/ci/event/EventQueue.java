package no.lundesgaard.ci.event;

import java.util.LinkedList;
import java.util.Queue;

public class EventQueue {
	private final Queue<Event> queue = new LinkedList<>();

	public boolean isNotEmpty() {
		return !queue.isEmpty();
	}

	public Event remove() {
		return queue.remove();
	}

	public void add(Event event) {
		queue.add(event);
	}
}
