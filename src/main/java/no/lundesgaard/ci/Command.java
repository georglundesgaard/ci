package no.lundesgaard.ci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.file.Files.delete;
import static java.nio.file.Files.list;
import static java.util.stream.Collectors.toList;

public abstract class Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(Command.class);

    public static Command from(Path commandPath) throws IOException {
        Type type = Type.from(commandPath);
        switch (type) {
            case SHUTDOWN:
                return ShutdownCommand.INSTANCE;
            case CREATE:
                return CreateCommand.from(commandPath);
            default:
                throw new UnsupportedOperationException("Command type <" + type + "> not implemented");
        }
    }

    public static Command nextFrom(Path commandsPath) throws IOException {
        List<Path> commandPaths = listCommands(commandsPath);
        for (Path commandPath : commandPaths) {
            try {
                Command command = from(commandPath);
                if (command != null) {
                    return command;
                }
            } finally {
                delete(commandPath);
            }
        }
        return null;
    }

    private static List<Path> listCommands(Path commandsPath) throws IOException {
        try (Stream<Path> commandPathStream = list(commandsPath)) {
            return commandPathStream.collect(toList());
        }
    }

    public abstract void execute(Ci ci);

    public abstract void validate();

    public abstract Type type();

    public enum Type {
        SHUTDOWN,
        CREATE;

        public static Type from(Path commandPath) {
            String command = commandPath.getFileName().toString().toUpperCase();
            try {
                return valueOf(command);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown command <" + command + ">");
            }
        }
    }
}
