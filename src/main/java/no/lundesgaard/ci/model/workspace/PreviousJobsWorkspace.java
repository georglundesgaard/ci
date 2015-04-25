package no.lundesgaard.ci.model.workspace;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.job.Job;
import org.apache.commons.lang3.NotImplementedException;

import java.nio.file.Path;

public class PreviousJobsWorkspace extends Workspace {
	public static final PreviousJobsWorkspace INSTANCE = new PreviousJobsWorkspace();

	private PreviousJobsWorkspace() {
	}

	@Override
	public Path init(Ci ci, Job job) {
		Path workspacePath = createWorkspace(ci, job.id);
		if (job.previousJobs.size() == 1) {
			Job previousJob = ci.jobs().job(job.previousJobs.iterator().next());
			previousJob.copyWorkspaceTo(ci, workspacePath);
			return workspacePath;
		}
		// TODO
		throw new NotImplementedException("TODO - jobs.previousJobs.size() > 1");
	}
}
