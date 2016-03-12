package no.lundesgaard.ci.events.assembler;

import no.lundesgaard.ci.events.model.EventType;
import no.lundesgaard.ci.events.resource.EventTypeResource;

import org.springframework.stereotype.Component;

@Component
public class EventTypeAssembler {
	public EventType toEntity(EventTypeResource eventTypeResource) {
		return new EventType(eventTypeResource.getName());
	}
}
