package no.lundesgaard.ci.model.workspace;

import no.lundesgaard.ci.Ci;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

public interface Workspace extends Serializable {
	Path init(Ci ci, String workspaceName);
}
