package no.lundesgaard.ci.data;

import no.lundesgaard.ci.Ci;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

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

	public void scan(Ci ci) {
		List<Repository> scannedRepositories = stream()
				.filter(repository -> repository.nodeId.equals(ci.nodeId()) && repository.readyForScan())
				.map(repository -> repository.scan(ci))
				.collect(toList());
		scannedRepositories.forEach(this::repository);
	}

	public int count() {
		return repositoryMap.size();
	}
}
