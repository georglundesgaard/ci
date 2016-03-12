package no.lundesgaard.ci.repositories.assembler;

import no.lundesgaard.ci.repositories.model.Repository;
import no.lundesgaard.ci.repositories.resource.RepositoryResource;

import org.springframework.stereotype.Component;

@Component
public class RepositoryAssembler {
	public Repository toRepository(RepositoryResource repositoryResource) {
		return new Repository(repositoryResource.getName(), repositoryResource.getUrl());
	}
}
