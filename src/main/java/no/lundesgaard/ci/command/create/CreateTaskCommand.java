package no.lundesgaard.ci.command.create;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.task.Task;
import no.lundesgaard.ci.model.trigger.RepositoryUpdatedTrigger;
import no.lundesgaard.ci.model.trigger.TasksCompletedSuccessfullyTrigger;
import no.lundesgaard.ci.model.trigger.Trigger;
import no.lundesgaard.ci.model.trigger.TriggerType;
import no.lundesgaard.ci.model.workspace.ClonedRepositoryWorkspace;
import no.lundesgaard.ci.model.workspace.PreviousTasksWorkspace;
import no.lundesgaard.ci.model.workspace.Workspace;
import no.lundesgaard.ci.model.workspace.WorkspaceType;

import java.util.Properties;

import static java.lang.String.format;

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
			case TASKS_COMPLETED_SUCCESSFULLY:
				String[] taskNames = commandProperties.getProperty("trigger.tasks").split(",");
				this.trigger = new TasksCompletedSuccessfullyTrigger(taskNames);
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
			case PREVIOUS_TASKS:
				this.workspace = PreviousTasksWorkspace.INSTANCE;
				break;
			default:
				throw new IllegalArgumentException(format("Unknown workspace type: %s", workspaceType));
		}
		this.script = commandProperties.getProperty("script");
	}

	@Override
	public void execute(Ci ci) {
		Task task = new Task(name, trigger, workspace, script);
		ci.addTask(task);
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
				&& ((RepositoryUpdatedTrigger) trigger).repository == null) {
			throw new IllegalStateException("missing repository name on trigger");
		}
		if (trigger instanceof TasksCompletedSuccessfullyTrigger
				&& ((TasksCompletedSuccessfullyTrigger) trigger).tasks.size() == 0) {
			throw new IllegalStateException("missing task names on trigger");
		}
	}

	private void validateWorkspace() {
		if (workspace instanceof ClonedRepositoryWorkspace
				&& ((ClonedRepositoryWorkspace) workspace).repository == null) {
			throw new IllegalStateException("missing repository name on workspace");
		}
	}

	private void validateScript() {
		if (script == null) {
			throw new IllegalStateException("missing script");
		}
	}
}
