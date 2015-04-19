package no.lundesgaard.ci.command.shutdown;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.command.Command;
import no.lundesgaard.ci.command.CommandType;

public class ShutdownCommand extends Command {
    public static final ShutdownCommand INSTANCE = new ShutdownCommand();

    private ShutdownCommand() {
    }

    @Override
    public void execute(Ci ci) {
        throw new UnsupportedOperationException("shutdown command is not executable");
    }

    @Override
    public void validate() {
        throw new UnsupportedOperationException("shutdown command is not executable");
    }

    @Override
    public CommandType type() {
        return CommandType.SHUTDOWN;
    }
}
