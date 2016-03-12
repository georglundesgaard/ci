package no.lundesgaard.ci.repositories.assembler;

import no.lundesgaard.ci.repositories.model.Author;
import no.lundesgaard.ci.repositories.model.Commit;
import no.lundesgaard.ci.repositories.resource.AuthorResource;
import no.lundesgaard.ci.repositories.resource.CommitResource;

import org.springframework.stereotype.Component;

@Component
public class CommitResourceAssembler {
	public CommitResource toResource(Commit commit) {
		return new CommitResource(commit.getCommitId(), authorResource(commit.getAuthor()), commit.getCommitDateTime(), commit.getFullMessage());
	}

	private AuthorResource authorResource(Author author) {
		return new AuthorResource(author.getName(), author.getEmail());
	}
}
