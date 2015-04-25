package no.lundesgaard.ci.model.workspace;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.job.Job;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.walkFileTree;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public abstract class Workspace implements Serializable {
	private static final Logger LOGGER = LoggerFactory.getLogger(Workspace.class);

	public abstract Path init(Ci ci, Job job);

	protected Path createWorkspace(Ci ci, String workspaceName) {
		Path workspacePath = ci.workspacesPath.resolve(workspaceName);
		try {
			if (exists(workspacePath)) {
				walkFileTree(workspacePath, deleteFileVisitor());
				LOGGER.warn("Deleted existing workspace: {}", workspacePath.getFileName());
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
				.toString();
	}
}
