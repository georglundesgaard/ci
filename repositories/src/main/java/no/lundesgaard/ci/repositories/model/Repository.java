package no.lundesgaard.ci.repositories.model;

public class Repository {
	private String name;
	private String url;
	private Commit lastCommit;

	public Repository() {
	}

	public Repository(Repository repository, Commit lastCommit) {
		this.name = repository.name;
		this.url = repository.url;
		this.lastCommit = lastCommit;
	}

	public Repository(String name, String url) {
		this.name = name;
		this.url = url;
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

	public Commit getLastCommit() {
		return lastCommit;
	}

	public void setLastCommit(Commit lastCommit) {
		this.lastCommit = lastCommit;
	}
}
