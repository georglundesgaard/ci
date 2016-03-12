package no.lundesgaard.ci.events.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

import no.lundesgaard.ci.events.assembler.EventAssembler;
import no.lundesgaard.ci.events.assembler.EventResourceAssembler;
import no.lundesgaard.ci.events.model.Event;
import no.lundesgaard.ci.events.resource.EventResource;
import no.lundesgaard.ci.events.resource.EventsResource;
import no.lundesgaard.ci.events.service.EventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ExposesResourceFor(Event.class)
@RequestMapping("/")
public class EventsController {
	@Autowired
	private EventService eventService;
	@Autowired
	private EventResourceAssembler resourceAssembler;
	@Autowired
	private EventAssembler eventAssembler;

	@RequestMapping(method = GET)
	@ResponseBody
	public ResourceSupport events() {
		EventsResource resource = new EventsResource(eventService.eventCount());
		resource.add(linkTo(EventsController.class).withSelfRel());
		return resource;
	}

	@RequestMapping(method = POST)
	public ResponseEntity<?> createEvent(@RequestBody EventResource eventResource) {
		String id = eventService.createEvent(eventAssembler.toEntity(eventResource));
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setLocation(fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri());
		return new ResponseEntity<>(null, httpHeaders, CREATED);
	}

	@RequestMapping(path = "/{id}", method = GET)
	public HttpEntity<EventResource> event(@PathVariable String id) {
		Event event = eventService.findEventById(id);
		if (event == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(resourceAssembler.toResource(event), OK);
	}
}
