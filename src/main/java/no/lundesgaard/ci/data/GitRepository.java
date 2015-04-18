package no.lundesgaard.ci.data;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.event.RepositoryUpdatedEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Instant;

import static java.lang.ProcessBuilder.Redirect.appendTo;
import static java.lang.String.format;
import static java.nio.file.Files.list;
import static java.time.Instant.now;

public class GitRepository extends Repository {
	private final String lastCommitId;

	public GitRepository(String name, String url, String nodeId) {
		super(name, url, nodeId);
		this.lastCommitId = null;
	}

	public GitRepository(GitRepository repository, Instant lastScan) {
		super(repository, lastScan);
		this.lastCommitId = repository.lastCommitId;
	}

	public GitRepository(GitRepository repository, Instant lastScan, String lastCommitId) {
		super(repository, lastScan);
		this.lastCommitId = lastCommitId;
	}

	@Override
	public Repository scan(Ci ci) {
		Path repositoriesPath = ci.getRepositoriesPath();
		Path repositoryPath =  ci.createRepositoryDirectoryIfNotExists(this);
		File outputLog = repositoriesPath.resolve(name + ".log").toFile();
		if (isEmpty(repositoryPath)) {
			cloneRepository(repositoryPath, outputLog);
		} else {
			updateRepository(repositoryPath, outputLog);
		}
		String lastCommitId = findLastCommitId(repositoryPath);
		if (!lastCommitId.equals(this.lastCommitId)) {
			ci.publishEvent(new RepositoryUpdatedEvent(this.name));
			return new GitRepository(this, now(), lastCommitId);
		}
		return new GitRepository(this, now());
	}

	private boolean isEmpty(Path repositoryPath) {
		try {
			return list(repositoryPath).count() == 0;
		} catch (IOException e) {
			throw new UncheckedIOException(format("Failed to list path <%s>", repositoryPath), e);
		}
	}

	private void updateRepository(Path repositoryPath, File outputLog) {
		processRepository(repositoryPath, outputLog, "update", "git", "pull");
	}

	private void cloneRepository(Path repositoryPath, File outputLog) {
		processRepository(repositoryPath, outputLog, "clone", "git", "clone", url, ".");
	}

	private void processRepository(Path repositoryPath, File outputLog, String commandType, String... command) {
		Process process;
		try {
			process = new ProcessBuilder(command)
					.directory(repositoryPath.toFile())
					.redirectOutput(appendTo(outputLog))
					.redirectError(appendTo(outputLog))
					.start();
		} catch (IOException e) {
			throw new UncheckedIOException(
					format("I/O error for process to %s repo <%s> with url <%s>", commandType, name, url), e);
		}
		try {
			int exitCode = process.waitFor();
			if (exitCode != 0) {
				throw new RuntimeException(
						format("Process to %s repo <%s> failed with exit code: <%d>", commandType, name, exitCode));
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(format("Process to %s repo <%s> interrupted", commandType, name), e);
		}
	}

	private String findLastCommitId(Path repositoryPath) {
		Process process;
		try {
			process = new ProcessBuilder("git", "rev-parse", "--short", "HEAD")
					.directory(repositoryPath.toFile())
					.start();
		} catch (IOException e) {
			throw new UncheckedIOException(
					format("I/O error for process to read last commit id for repo <%s> with url <%s>",
							name, url), e);
		}
		try (BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			return stdout.readLine();
		} catch (IOException e) {
			throw new UncheckedIOException(
					format("I/O error for process to read last commit id for repo <%s> with url <%s>",
							name, url), e);
		}
	}
}
