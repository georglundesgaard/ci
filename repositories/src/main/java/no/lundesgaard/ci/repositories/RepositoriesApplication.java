package no.lundesgaard.ci.repositories;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.hateoas.config.EnableEntityLinks;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAutoConfiguration
@EnableEntityLinks
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {
		"no.lundesgaard.ci.repositories.assembler",
		"no.lundesgaard.ci.repositories.controller",
		"no.lundesgaard.ci.repositories.mapper",
		"no.lundesgaard.ci.repositories.service"
})
public class RepositoriesApplication {
	public static void main(String[] args) throws Exception {
		SpringApplication.run(RepositoriesApplication.class, args);
	}
}
