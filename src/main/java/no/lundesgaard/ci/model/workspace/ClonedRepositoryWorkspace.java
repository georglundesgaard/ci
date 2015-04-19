package no.lundesgaard.ci.model.workspace;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.repository.Repository;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.createDirectory;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class ClonedRepositoryWorkspace implements Workspace {
	public final String repository;

	public ClonedRepositoryWorkspace(String repository) {
		this.repository = repository;
	}

	@Override
	public Path init(Ci ci, Path workspacePath) throws IOException {
		Repository repository = ci.repositories().repository(this.repository);
		repository.copyToWorkspace(ci, workspacePath);
		return workspacePath;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("repository", repository)
				.toString();
	}
}
