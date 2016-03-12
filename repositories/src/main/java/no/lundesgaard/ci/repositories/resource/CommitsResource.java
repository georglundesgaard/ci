package no.lundesgaard.ci.repositories.resource;

import java.util.List;

import org.springframework.hateoas.ResourceSupport;

public class CommitsResource extends ResourceSupport {
	private List<CommitResource> commits;

	public CommitsResource(List<CommitResource> commits) {
		this.commits = commits;
	}

	public List<CommitResource> getCommits() {
		return commits;
	}
}
