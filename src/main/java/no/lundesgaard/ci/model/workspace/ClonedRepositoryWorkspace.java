package no.lundesgaard.ci.model.workspace;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.repository.Repository;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.nio.file.Path;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class ClonedRepositoryWorkspace implements Workspace {
	public final String repositoryName;

	public ClonedRepositoryWorkspace(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	@Override
	public Path init(Ci ci, String workspaceName) {
		Repository repository = ci.repositories().repository(this.repositoryName);
		Path workspacePath = ci.workspacesPath.resolve(workspaceName);
		repository.copyToWorkspace(ci, workspacePath);
		return workspacePath;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("repositoryName", repositoryName)
				.toString();
	}
}
