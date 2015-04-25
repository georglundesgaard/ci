package no.lundesgaard.ci.model.command.show;

import no.lundesgaard.ci.model.command.Command;
import no.lundesgaard.ci.model.command.CommandType;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static no.lundesgaard.ci.model.command.CommandType.SHOW;

public abstract class ShowCommand extends Command {
	@Override
	public CommandType type() {
		return SHOW;
	}

	public static ShowCommand from(Path showCommandPath) throws IOException {
		Properties commandProperties = new Properties();
		try (FileReader commandReader = new FileReader(showCommandPath.toFile())) {
			commandProperties.load(commandReader);
		}
		String type = commandProperties.getProperty("type", "unknown");
		switch (type) {
			case "latest-job":
				return new ShowLatestJobCommand(commandProperties);
			default:
				throw new IllegalArgumentException("invalid show command (type=" + type + ")");
		}
	}
}
