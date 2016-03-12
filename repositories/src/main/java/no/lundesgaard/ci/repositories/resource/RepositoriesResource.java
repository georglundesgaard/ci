package no.lundesgaard.ci.repositories.resource;

import java.util.List;

import org.springframework.hateoas.ResourceSupport;

public class RepositoriesResource extends ResourceSupport {
	private List<String> repositories;

	public RepositoriesResource(List<String> repositories) {
		this.repositories = repositories;
	}

	public List<String> getRepositories() {
		return repositories;
	}

	public void setRepositories(List<String> repositories) {
		this.repositories = repositories;
	}
}
