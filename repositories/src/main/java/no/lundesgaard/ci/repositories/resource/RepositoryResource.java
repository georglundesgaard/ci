package no.lundesgaard.ci.repositories.resource;

import org.springframework.hateoas.ResourceSupport;

public class RepositoryResource extends ResourceSupport {
	private String name;
	private String url;
	private CommitResource lastCommit;

	public RepositoryResource() {
	}

	public RepositoryResource(String name, String url, CommitResource lastCommit) {
		this.name = name;
		this.url = url;
		this.lastCommit = lastCommit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public CommitResource getLastCommit() {
		return lastCommit;
	}

	public void setLastCommit(CommitResource lastCommit) {
		this.lastCommit = lastCommit;
	}
}
