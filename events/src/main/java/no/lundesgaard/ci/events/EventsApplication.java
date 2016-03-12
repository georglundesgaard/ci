package no.lundesgaard.ci.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.hateoas.config.EnableEntityLinks;

@EnableAutoConfiguration
@EnableEntityLinks
@ComponentScan(basePackages = {
		"no.lundesgaard.ci.events.assembler",
		"no.lundesgaard.ci.events.controller",
		"no.lundesgaard.ci.events.mapper",
		"no.lundesgaard.ci.events.service"
})
public class EventsApplication {
	public static void main(String[] args) {
		SpringApplication.run(EventsApplication.class, args);
	}
}
