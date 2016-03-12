package no.lundesgaard.ci.repositories.service;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import no.lundesgaard.ci.repositories.mapper.RevCommitToCommitMapper;
import no.lundesgaard.ci.repositories.model.Commit;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GitService {
	@Autowired
	private RevCommitToCommitMapper revCommitToCommitMapper;

	@Value("${services.git.repositoriesRoot}")
	private String repositoriesRoot;

	public boolean repositoryExists(String repositoryName) {
		File repositoryLocation = location(repositoryName);
		return repositoryLocation.exists() && repositoryLocation.isDirectory();
	}

	public void cloneRepository(String repositoryUrl, String repositoryName) {
		try (Git git = Git.cloneRepository().setURI(repositoryUrl).setDirectory(location(repositoryName)).call()) {
			git.getRepository().close();
		} catch (GitAPIException e) {
			throw new GitServiceException(format("Failed to clone repository <%s> from <%s>: %s", repositoryName, repositoryUrl, e.getMessage()), e);
		}
	}

	public List<Commit> pullAndGetCommitLogFor(String repositoryName) {
		try (Git git = Git.open(location(repositoryName))) {
			git.pull().call();
			List<Commit> commits = revCommitStream(git).map(revCommitToCommitMapper::commit).collect(toList());
			git.getRepository().close();
			return commits;
		} catch (IOException | GitAPIException e) {
			throw new GitServiceException(format("Failed pull and get commit log for repository <%s>: %s", repositoryName, e.getMessage()), e);
		}
	}

	public Commit pullAndGetLastCommitFor(String repositoryName) {
		try (Git git = Git.open(location(repositoryName))) {
			git.pull().call();
			Commit commit = revCommitToCommitMapper.commit(git.log().call().iterator().next());
			git.getRepository().close();
			return commit;
		} catch (IOException | GitAPIException e) {
			throw new GitServiceException(format("Failed pull and get last commit for repository <%s>: %s", repositoryName, e.getMessage()), e);
		}
	}

	private File location(String repositoryName) {
		return new File(repositoriesRoot, repositoryName);
	}

	private Stream<RevCommit> revCommitStream(Git git) throws GitAPIException {
		return StreamSupport.stream(git.log().call().spliterator(), false);
	}
}
