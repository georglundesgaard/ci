package no.lundesgaard.ci.model.repository;

import no.lundesgaard.ci.Ci;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.walkFileTree;
import static java.time.Instant.now;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public abstract class Repository implements Serializable {
	public final String name;
	public final String url;
	public final String nodeId;
	public final Duration scanInterval = Duration.ofMinutes(1);
	public final Instant lastScan;
	public final String lastError;

	public Repository(String name, String url, String nodeId) {
		this.name = name;
		this.url = url;
		this.nodeId = nodeId;
		this.lastScan = null;
		this.lastError = null;
	}

	public Repository(Repository repository, Instant lastScan, String lastError) {
		this.name = repository.name;
		this.url = repository.url;
		this.nodeId = repository.nodeId;
		this.lastScan = lastScan;
		this.lastError = lastError;
	}

	public boolean readyForScan() {
		return lastScan == null || now().isAfter(lastScan.plus(scanInterval));
	}

	public abstract Repository scan(Ci ci);

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("name", name)
				.append("url", url)
				.append("nodeId", nodeId)
				.append("scanInterval", scanInterval)
				.append("lastScan", lastScan)
				.toString();
	}

	public void copyToWorkspace(Ci ci, Path workspacePath) {
		Path repositoryPath = ci.repositoriesPath.resolve(name);
		try {
			walkFileTree(repositoryPath, copyFileVisitor(repositoryPath, workspacePath));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private FileVisitor<Path> copyFileVisitor(Path source, Path target) {
		return new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				copy(file, target.resolve(source.relativize(file)));
				return CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Path targetDir = target.resolve(source.relativize(dir));
				try {
					copy(dir, targetDir);
				} catch (FileAlreadyExistsException e) {
					if (!isDirectory(targetDir))
						throw e;
				}
				return CONTINUE;
			}
		};
	}
}
