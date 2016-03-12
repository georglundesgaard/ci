package no.lundesgaard.ci.repositories.service;

import java.net.URI;

import no.lundesgaard.ci.repositories.model.RepositoryUpdateEvent;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EventService {
	public void createEvent(RepositoryUpdateEvent event) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<RepositoryUpdateEvent> request = new HttpEntity<>(event, httpHeaders);
		URI location = new RestTemplate().postForLocation("http://events:8080/events/", request);
		System.err.println(location);
	}
}
