package no.lundesgaard.ci.events.assembler;

import no.lundesgaard.ci.events.controller.EventTypesController;
import no.lundesgaard.ci.events.model.EventType;
import no.lundesgaard.ci.events.resource.EventTypeResource;

import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class EventTypeResourceAssembler extends ResourceAssemblerSupport<EventType, EventTypeResource> {
	public EventTypeResourceAssembler() {
		super(EventTypesController.class, EventTypeResource.class);
	}

	@Override
	public EventTypeResource toResource(EventType eventType) {
		return createResourceWithId(eventType.getName(), eventType);
	}

	@Override
	protected EventTypeResource instantiateResource(EventType eventType) {
		return new EventTypeResource(eventType.getName());
	}
}
