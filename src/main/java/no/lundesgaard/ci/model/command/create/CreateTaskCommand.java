package no.lundesgaard.ci.model.command.create;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.task.Task;
import no.lundesgaard.ci.model.task.TaskId;
import no.lundesgaard.ci.model.trigger.RepositoryUpdatedTrigger;
import no.lundesgaard.ci.model.trigger.JobsCompletedSuccessfullyTrigger;
import no.lundesgaard.ci.model.trigger.Trigger;
import no.lundesgaard.ci.model.trigger.TriggerType;
import no.lundesgaard.ci.model.workspace.ClonedRepositoryWorkspace;
import no.lundesgaard.ci.model.workspace.PreviousJobsWorkspace;
import no.lundesgaard.ci.model.workspace.Workspace;
import no.lundesgaard.ci.model.workspace.WorkspaceType;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Properties;

import static java.lang.String.format;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class CreateTaskCommand extends CreateCommand {
	private final String name;
	private final Trigger trigger;
	private final Workspace workspace;
	private final String script;

	public CreateTaskCommand(Properties commandProperties) {
		this.name = commandProperties.getProperty("name");
		TriggerType triggerType = TriggerType.valueOf(commandProperties.getProperty("trigger.type"));
		switch (triggerType) {
			case REPOSITORY_UPDATED:
				String repositoryName = commandProperties.getProperty("trigger.repository");
				this.trigger = new RepositoryUpdatedTrigger(repositoryName);
				break;
			case JOBS_COMPLETED_SUCCESSFULLY:
				String[] taskNames = commandProperties.getProperty("trigger.tasks").split(",");
				this.trigger = new JobsCompletedSuccessfullyTrigger(taskIds(taskNames));
				break;
			default:
				throw new IllegalArgumentException(format("Unknown trigger type: %s", triggerType));
		}
		WorkspaceType workspaceType = WorkspaceType.valueOf(commandProperties.getProperty("workspace.type"));
		switch (workspaceType) {
			case CLONED_REPOSITORY:
				String repositoryName = commandProperties.getProperty("workspace.repository");
				this.workspace = new ClonedRepositoryWorkspace(repositoryName);
				break;
			case PREVIOUS_JOBS:
				this.workspace = PreviousJobsWorkspace.INSTANCE;
				break;
			default:
				throw new IllegalArgumentException(format("Unknown workspace type: %s", workspaceType));
		}
		this.script = commandProperties.getProperty("script");
	}

	private TaskId[] taskIds(String[] taskNames) {
		TaskId[] taskIds = new TaskId[taskNames.length];
		for (int i = 0; i < taskNames.length; i++) {
			taskIds[i] = TaskId.taskId(taskNames[i]);
		}
		return taskIds;
	}

	@Override
	public void execute(Ci ci) {
		Task task = new Task(name, trigger, workspace, script);
		ci.tasks().task(task);
	}

	@Override
	public void validate() {
		validateName();
		validateTrigger();
		validateWorkspace();
		validateScript();
	}

	private void validateName() {
		if (name == null) {
			throw new IllegalStateException("missing name");
		}
	}

	private void validateTrigger() {
		if (trigger instanceof RepositoryUpdatedTrigger
				&& ((RepositoryUpdatedTrigger) trigger).repositoryName == null) {
			throw new IllegalStateException("missing repository name on trigger");
		}
		if (trigger instanceof JobsCompletedSuccessfullyTrigger
				&& ((JobsCompletedSuccessfullyTrigger) trigger).taskIds.size() == 0) {
			throw new IllegalStateException("missing task names on trigger");
		}
	}

	private void validateWorkspace() {
		if (workspace instanceof ClonedRepositoryWorkspace
				&& ((ClonedRepositoryWorkspace) workspace).repositoryName == null) {
			throw new IllegalStateException("missing repository name on workspace");
		}
	}

	private void validateScript() {
		if (script == null) {
			throw new IllegalStateException("missing script");
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("name", name)
				.append("trigger", trigger)
				.append("workspace", workspace)
				.toString();
	}
}
