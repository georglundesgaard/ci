package no.lundesgaard.ci.model.command;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.command.create.CreateCommand;
import no.lundesgaard.ci.model.command.list.ListCommand;
import no.lundesgaard.ci.model.command.show.ShowCommand;
import no.lundesgaard.ci.model.command.shutdown.RestartCommand;
import no.lundesgaard.ci.model.command.shutdown.ShutdownCommand;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.file.Files.delete;
import static java.nio.file.Files.list;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public abstract class Command {
    public static Command from(Path commandPath) throws IOException {
        CommandType type = CommandType.from(commandPath);
        switch (type) {
            case SHUTDOWN:
                return ShutdownCommand.INSTANCE;
            case RESTART:
                return RestartCommand.INSTANCE;
            case CREATE:
                return CreateCommand.from(commandPath);
            case LIST:
                return ListCommand.from(commandPath);
            case SHOW:
                return ShowCommand.from(commandPath);
            default:
                throw new UnsupportedOperationException("Command type <" + type + "> not implemented");
        }
    }

    public static Command nextFrom(Path commandsPath) {
        try {
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
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<Path> listCommands(Path commandsPath) throws IOException {
        try (Stream<Path> commandPathStream = list(commandsPath)) {
            return commandPathStream.collect(toList());
        }
    }

    public abstract void execute(Ci ci);

    public abstract void validate();

    public abstract CommandType type();

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .toString();
    }
}
