package no.lundesgaard.ci.repositories.assembler;

import no.lundesgaard.ci.repositories.controller.RepositoriesController;
import no.lundesgaard.ci.repositories.model.Commit;
import no.lundesgaard.ci.repositories.model.Repository;
import no.lundesgaard.ci.repositories.resource.CommitResource;
import no.lundesgaard.ci.repositories.resource.RepositoryResource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class RepositoryResourceAssembler extends ResourceAssemblerSupport<Repository, RepositoryResource> {
	@Autowired
	private CommitResourceAssembler commitResourceAssembler;

	public RepositoryResourceAssembler() {
		super(RepositoriesController.class, RepositoryResource.class);
	}

	@Override
	public RepositoryResource toResource(Repository repository) {
		return createResourceWithId(repository.getName(), repository);
	}

	@Override
	protected RepositoryResource instantiateResource(Repository repository) {
		return new RepositoryResource(repository.getName(), repository.getUrl(), commitResource(repository.getLastCommit()));
	}

	private CommitResource commitResource(Commit lastCommit) {
		if (lastCommit == null) {
			return null;
		}
		return commitResourceAssembler.toResource(lastCommit);
	}
}
