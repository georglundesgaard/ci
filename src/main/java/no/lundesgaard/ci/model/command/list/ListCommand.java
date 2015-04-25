package no.lundesgaard.ci.model.command.list;

import no.lundesgaard.ci.model.command.Command;
import no.lundesgaard.ci.model.command.CommandType;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static no.lundesgaard.ci.model.command.CommandType.LIST;

public abstract class ListCommand extends Command {
	@Override
	public CommandType type() {
		return LIST;
	}

	public static ListCommand from(Path listCommandPath) throws IOException {
		Properties commandProperties = new Properties();
		try (FileReader commandReader = new FileReader(listCommandPath.toFile())) {
			commandProperties.load(commandReader);
		}
		String type = commandProperties.getProperty("type", "unknown");
		switch (type) {
			case "git-repo":
				return ListGitRepoCommand.INSTANCE;
			case "task":
				return ListTaskCommand.INSTANCE;
			case "jobs":
				return new ListJobsCommand(commandProperties);
			default:
				throw new IllegalArgumentException("invalid create command (type=" + type + ")");
		}
	}
}
