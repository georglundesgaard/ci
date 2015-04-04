package no.lundesgaard.ci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public abstract class Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(Command.class);

    public static Command from(File commandFile) {
        String commandName = commandFile.getName();
        if ("shutdown".equalsIgnoreCase(commandName)) {
            return ShutdownCommand.INSTANCE;
        } else {
            LOGGER.debug("Unknown command: {}", commandName);
        }
        return null;
    }

    public static Command nextFrom(File commandsDir) {
        if (!commandsDir.exists()) {
            return null;
        }
        File[] commandFiles = commandsDir.listFiles();
        if (commandFiles == null) {
            LOGGER.warn("Failed to list files in {} (isDirectory={})", commandsDir, commandsDir.isDirectory());
            return null;
        }
        for (File commandFile : commandFiles) {
            Command command = Command.from(commandFile);
            deleteFile(commandFile);
            if (command != null) {
                return command;
            }
        }
        return null;
    }

    private static void deleteFile(File file) {
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            LOGGER.warn("Failed to delete file: {}", file, e);
        }
    }
}
