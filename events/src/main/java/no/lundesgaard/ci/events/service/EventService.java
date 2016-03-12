package no.lundesgaard.ci.events.service;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import no.lundesgaard.ci.events.model.Event;
import no.lundesgaard.ci.events.model.EventType;

import org.springframework.stereotype.Service;

@Service
public class EventService {
	private Map<String, Event> eventMap = new HashMap<>();
	private Map<String, EventType> eventTypeMap = new HashMap<>();
	{
		eventTypeMap.put("repository-update", new EventType("repository-update"));
	}

	public int eventCount() {
		return eventMap.size();
	}

	public String createEvent(Event event) {
		event.setId(UUID.randomUUID().toString());
		eventMap.put(event.getId(), event);
		return event.getId();
	}

	public Event findEventById(String id) {
		return eventMap.get(id);
	}

	public List<String> findAllEventTypeNames() {
		return eventTypeMap.values().stream().map(EventType::getName).collect(toList());
	}

	public String createEventType(EventType eventType) {
		eventTypeMap.put(eventType.getName(), eventType);
		return eventType.getName();
	}

	public EventType findEventTypeByName(String name) {
		return eventTypeMap.get(name);
	}
}
