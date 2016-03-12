package no.lundesgaard.ci.repositories.service;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.lundesgaard.ci.repositories.model.Commit;
import no.lundesgaard.ci.repositories.model.Repository;
import no.lundesgaard.ci.repositories.model.RepositoryUpdateEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RepositoryService {
	private Map<String, Repository> repositoryMap = new HashMap<>();
	{
		Repository repository = new Repository("simply-ci", "https://github.com/georglundesgaard/simply-ci.git");
		repositoryMap.put(repository.getName(), repository);
	}

	@Autowired
	private GitService gitService;
	@Autowired
	private EventService eventsService;

	public List<String> findAllRepositoryNames() {
		return repositoryMap.keySet().stream().collect(toList());
	}

	public String createRepository(Repository repository) {
		repositoryMap.put(repository.getName(), repository);
		return repository.getName();
	}

	public Repository findRepository(String name) {
		Repository repository = repositoryMap.get(name);
		if (repository == null) {
			return null;
		}
		cloneIfRepositoryDoesNotExists(repository);
		Commit lastCommit = gitService.pullAndGetLastCommitFor(name);
		return new Repository(repository, lastCommit);
	}

	private void cloneIfRepositoryDoesNotExists(Repository repository) {
		if (!gitService.repositoryExists(repository.getName())) {
			gitService.cloneRepository(repository.getUrl(), repository.getName());
		}
	}

	public List<Commit> findRepositoryCommits(String name) {
		if (repositoryMap.containsKey(name) && gitService.repositoryExists(name)) {
			return gitService.pullAndGetCommitLogFor(name);
		}
		return null;
	}
	
	@Scheduled(fixedDelay = 5000)
	public void checkForNewCommits() {
		repositoryMap.forEach((name, repository) -> {
			cloneIfRepositoryDoesNotExists(repository);
			Commit lastCommit = gitService.pullAndGetLastCommitFor(name);
			Commit currentLastCommit = repository.getLastCommit();
			if (currentLastCommit == null || !currentLastCommit.equals(lastCommit)) {
				repository.setLastCommit(lastCommit);
				eventsService.createEvent(new RepositoryUpdateEvent(name));
			}
		});
	}
}
