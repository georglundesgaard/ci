package no.lundesgaard.ci.model.repository;

import no.lundesgaard.ci.Ci;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class Repositories {
	private final Map<String, Repository> repositoryMap;

	public Repositories(Map<String, Repository> repositoryMap) {
		this.repositoryMap = repositoryMap;
	}

	public Repository repository(String name) {
		return repositoryMap.get(name);
	}

	public Repository repository(Repository repository) {
		repositoryMap.put(repository.name, repository);
		return repository;
	}

	public Stream<Repository> stream() {
		return repositoryMap.values().stream();
	}

	public int scan(Ci ci, Consumer<Repository> onRepositoryUpdated) {
		int[] scannedRepositories = { 0 };
		stream()
				.filter(repository -> repository.nodeId.equals(ci.nodeId()) && repository.readyForScan())
				.map(repository -> repository.scan(ci, onRepositoryUpdated))
				.forEach(repository -> {
					repository(repository);
					scannedRepositories[0]++;
				});
		return scannedRepositories[0];
	}

	public int count() {
		return repositoryMap.size();
	}
}
