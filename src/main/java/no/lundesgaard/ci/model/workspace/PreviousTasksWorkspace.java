package no.lundesgaard.ci.model.workspace;

import no.lundesgaard.ci.Ci;

import java.nio.file.Path;

public class PreviousTasksWorkspace implements Workspace {
	public static final PreviousTasksWorkspace INSTANCE = new PreviousTasksWorkspace();

	private PreviousTasksWorkspace() {
	}

	@Override
	public Path init(Ci ci, String workspaceName) {
		// TODO
		return null;
	}
}
