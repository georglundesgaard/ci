package no.lundesgaard.ci.model.workspace;

import no.lundesgaard.ci.Ci;

import java.io.IOException;
import java.nio.file.Path;

public class PreviousTasksWorkspace implements Workspace {
	public static final PreviousTasksWorkspace INSTANCE = new PreviousTasksWorkspace();

	private PreviousTasksWorkspace() {
	}

	@Override
	public Path init(Ci ci, Path workspacePath) throws IOException {
		// TODO
		return null;
	}
}
