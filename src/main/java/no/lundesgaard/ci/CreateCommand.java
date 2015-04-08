package no.lundesgaard.ci;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public abstract class CreateCommand extends Command {
    @Override
    public Type type() {
        return Type.CREATE;
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
                return CreateTaskCommand.from(commandProperties);
            default:
                throw new IllegalArgumentException("invalid create command (type=" + type + ")");
        }
    }
}
