package no.lundesgaard.ci.events.assembler;

import no.lundesgaard.ci.events.model.Event;
import no.lundesgaard.ci.events.resource.EventResource;

import org.springframework.stereotype.Component;

@Component
public class EventAssembler {
	public Event toEntity(EventResource eventResource) {
		return new Event(eventResource.getType(), eventResource.getValue());
	}
}
