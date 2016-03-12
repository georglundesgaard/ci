package no.lundesgaard.ci.events.assembler;

import no.lundesgaard.ci.events.controller.EventsController;
import no.lundesgaard.ci.events.model.Event;
import no.lundesgaard.ci.events.resource.EventResource;

import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class EventResourceAssembler extends ResourceAssemblerSupport<Event, EventResource> {
	public EventResourceAssembler() {
		super(EventsController.class, EventResource.class);
	}

	@Override
	public EventResource toResource(Event event) {
		return createResourceWithId(event.getId(), event);
	}

	@Override
	protected EventResource instantiateResource(Event event) {
		return new EventResource(event.getId(), event.getType(), event.getValue());
	}
}
