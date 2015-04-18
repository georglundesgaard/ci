package no.lundesgaard.ci;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static no.lundesgaard.ci.Command.Type.LIST;

public abstract class ListCommand extends Command {
	@Override
	public Type type() {
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
				return new ListGitRepoCommand();
			default:
				throw new IllegalArgumentException("invalid create command (type=" + type + ")");
		}
	}
}
