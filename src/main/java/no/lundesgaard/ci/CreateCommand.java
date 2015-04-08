package no.lundesgaard.ci;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
        String subType = commandProperties.getProperty("sub-type", "unknown");
        switch (subType) {
            case "git-repo":
                return new CreateGitRepoCommand(commandProperties);
            default:
                throw new IllegalArgumentException("invalid create command (subType=" + subType + ")");
        }
    }
}
