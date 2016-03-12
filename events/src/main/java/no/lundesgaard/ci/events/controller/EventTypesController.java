package no.lundesgaard.ci.events.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

import java.util.List;

import no.lundesgaard.ci.events.assembler.EventTypeAssembler;
import no.lundesgaard.ci.events.assembler.EventTypeResourceAssembler;
import no.lundesgaard.ci.events.model.EventType;
import no.lundesgaard.ci.events.resource.EventTypeResource;
import no.lundesgaard.ci.events.resource.EventTypesResource;
import no.lundesgaard.ci.events.service.EventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ExposesResourceFor(EventType.class)
@RequestMapping("/types")
public class EventTypesController {
	@Autowired
	private EventService eventService;
	@Autowired
	private EventTypeResourceAssembler resourceAssembler;
	@Autowired
	private EventTypeAssembler eventTypeAssembler;
	@Autowired
	private EntityLinks entityLinks;


	@RequestMapping(method = GET)
	@ResponseBody
	public HttpEntity<EventTypesResource> eventTypes() {
		List<String> eventTypes = eventService.findAllEventTypeNames();
		EventTypesResource resource = new EventTypesResource(eventTypes);
		resource.add(entityLinks.linkToCollectionResource(EventType.class).withSelfRel());
		resource.add(eventTypes.stream().map(this::linkTo).toArray(Link[]::new));
		return new ResponseEntity<>(resource, OK);
	}

	private Link linkTo(String eventType) {
		return entityLinks.linkForSingleResource(EventType.class, eventType).withRel(eventType);
	}

	@RequestMapping(method = POST)
	public HttpEntity<?> createEventType(@RequestBody EventTypeResource eventTypeResource) {
		EventType eventType = eventTypeAssembler.toEntity(eventTypeResource);
		String name = eventService.createEventType(eventType);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setLocation(fromCurrentRequest().path("/{name}").buildAndExpand(name).toUri());
		return new ResponseEntity<>(null, httpHeaders, CREATED);
	}

	@RequestMapping(path = "/{name}", method = GET)
	public HttpEntity<EventTypeResource> eventType(@PathVariable String name) {
		EventType eventType = eventService.findEventTypeByName(name);
		if (eventType == null) {
			return new ResponseEntity<>(NOT_FOUND);
		}
		return new ResponseEntity<>(resourceAssembler.toResource(eventType), OK);
	}
}
