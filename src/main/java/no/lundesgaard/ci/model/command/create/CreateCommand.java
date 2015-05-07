package no.lundesgaard.ci.model.command.create;

import no.lundesgaard.ci.model.command.Command;
import no.lundesgaard.ci.model.command.CommandType;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static no.lundesgaard.ci.model.command.CommandType.CREATE;

public abstract class CreateCommand extends Command {
    @Override
    public CommandType type() {
        return CREATE;
    }

    public static CreateCommand from(Path createCommandPath) throws IOException {
        Properties commandProperties = new Properties();
        try (FileReader commandReader = new FileReader(createCommandPath.toFile())) {
            commandProperties.load(commandReader);
        }
        String type = commandProperties.getProperty("type", "unknown");
        switch (type) {
            case "git-repo":
                return new CreateGitRepoCommand(commandProperties);
            case "task":
                return new CreateTaskCommand(commandProperties);
            default:
                throw new IllegalArgumentException("invalid create command (type=" + type + ")");
        }
    }
}
