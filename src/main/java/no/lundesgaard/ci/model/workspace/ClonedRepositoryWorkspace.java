package no.lundesgaard.ci.model.workspace;

import no.lundesgaard.ci.Ci;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.walkFileTree;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class ClonedRepositoryWorkspace implements Workspace {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClonedRepositoryWorkspace.class);

	public final String repositoryName;

	public ClonedRepositoryWorkspace(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	@Override
	public Path init(Ci ci, String workspaceName) {
		Path workspacePath = createWorkspace(ci, workspaceName);
		ci.repositories()
				.repository(repositoryName)
				.copyToWorkspace(ci, workspacePath);
		return workspacePath;
	}

	private Path createWorkspace(Ci ci, String workspaceName) {
		Path workspacePath = ci.workspacesPath.resolve(workspaceName);
		try {
			if (exists(workspacePath)) {
				walkFileTree(workspacePath, deleteFileVisitor());
				LOGGER.warn("Deleted existing workspace: {}", workspacePath);
			}
			return createDirectory(workspacePath);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private FileVisitor<Path> deleteFileVisitor() {
		return new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				deleteIfExists(file);
				return CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				deleteIfExists(dir);
				return CONTINUE;
			}
		};
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("repositoryName", repositoryName)
				.toString();
	}
}
