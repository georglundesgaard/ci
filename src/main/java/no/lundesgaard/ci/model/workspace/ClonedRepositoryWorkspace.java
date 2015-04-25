package no.lundesgaard.ci.model.workspace;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.job.Job;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

public class ClonedRepositoryWorkspace extends Workspace {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClonedRepositoryWorkspace.class);

	public final String repositoryName;

	public ClonedRepositoryWorkspace(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	@Override
	public Path init(Ci ci, Job job) {
		Path workspacePath = createWorkspace(ci, job.id);
		ci.repositories()
				.repository(repositoryName)
				.copyToWorkspace(ci, workspacePath);
		return workspacePath;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("repositoryName", repositoryName)
				.toString();
	}
}
