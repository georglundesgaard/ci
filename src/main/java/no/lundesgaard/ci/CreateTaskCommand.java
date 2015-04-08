package no.lundesgaard.ci;

import java.util.Properties;

public abstract class CreateTaskCommand extends CreateCommand {
	protected final Trigger trigger;

	public CreateTaskCommand(Properties commandProperties) {
		this.trigger = Trigger.valueOf(commandProperties.getProperty("trigger"));
	}

	public static CreateTaskCommand from(Properties commandProperties) {
		String taskType = commandProperties.getProperty("task-type", "unknown");
		switch (taskType) {
			case "build":
				return new CreateBuildTaskCommand(commandProperties);
			default:
				throw new IllegalArgumentException("invalid create task command (taskType=" + taskType + ")");
		}
	}

}
