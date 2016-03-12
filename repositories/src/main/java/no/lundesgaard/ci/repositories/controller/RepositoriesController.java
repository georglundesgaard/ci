package no.lundesgaard.ci.repositories.controller;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

import java.util.List;

import no.lundesgaard.ci.repositories.assembler.CommitResourceAssembler;
import no.lundesgaard.ci.repositories.assembler.RepositoryAssembler;
import no.lundesgaard.ci.repositories.assembler.RepositoryResourceAssembler;
import no.lundesgaard.ci.repositories.model.Commit;
import no.lundesgaard.ci.repositories.model.Repository;
import no.lundesgaard.ci.repositories.resource.CommitsResource;
import no.lundesgaard.ci.repositories.resource.RepositoriesResource;
import no.lundesgaard.ci.repositories.resource.RepositoryResource;
import no.lundesgaard.ci.repositories.service.RepositoryService;

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
@ExposesResourceFor(Repository.class)
@RequestMapping("/")
public class RepositoriesController {
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private EntityLinks entityLinks;
	@Autowired
	private RepositoryResourceAssembler repositoryResourceAssembler;
	@Autowired
	private RepositoryAssembler repositoryAssembler;
	@Autowired
	private CommitResourceAssembler commitResourceAssembler;


	@RequestMapping(method = GET)
	@ResponseBody
	public RepositoriesResource repositories() {
		List<String> repositories = repositoryService.findAllRepositoryNames();
		RepositoriesResource resource = new RepositoriesResource(repositories);
		resource.add(entityLinks.linkToCollectionResource(Repository.class).withSelfRel());
		resource.add(repositories.stream().map(this::linkToRepository).toArray(Link[]::new));
		return resource;
	}

	private Link linkToRepository(String repository) {
		return entityLinks.linkForSingleResource(Repository.class, repository).withRel(repository);
	}

	@RequestMapping(method = POST)
	public HttpEntity<?> createRepository(@RequestBody RepositoryResource repositoryResource) {
		String name = repositoryService.createRepository(repositoryAssembler.toRepository(repositoryResource));
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setLocation(fromCurrentRequest().path("/{name}").buildAndExpand(name).toUri());
		return new ResponseEntity<>(null, httpHeaders, CREATED);
	}

	@RequestMapping(path = "/{repositoryName}", method = GET)
	public HttpEntity<RepositoryResource> repository(@PathVariable String repositoryName) {
		Repository repository = repositoryService.findRepository(repositoryName);
		if (repository == null) {
			new ResponseEntity<>(NOT_FOUND);
		}
		RepositoryResource resource = repositoryResourceAssembler.toResource(repository);
		resource.add(linkTo(RepositoriesController.class).withRel("parent"));
		resource.add(linkTo(RepositoriesController.class).slash(repositoryName).slash("log").withRel("log"));
		return new ResponseEntity<>(resource, OK);
	}

	@RequestMapping(path = "/{repositoryName}/log", method = GET)
	public HttpEntity<CommitsResource> commitsFor(@PathVariable String repositoryName) {
		List<Commit> commits = repositoryService.findRepositoryCommits(repositoryName);
		if (commits == null) {
			return new ResponseEntity<>(NOT_FOUND);
		}
		CommitsResource resource = new CommitsResource(commits.stream().map(commitResourceAssembler::toResource).collect(toList()));
		resource.add(linkTo(RepositoriesController.class).slash(repositoryName).slash("log").withSelfRel());
		resource.add(linkTo(RepositoriesController.class).slash(repositoryName).withRel("parent"));
		return new ResponseEntity<>(resource, OK);
	}}
