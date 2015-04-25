package no.lundesgaard.ci.model.command.shutdown;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.command.Command;
import no.lundesgaard.ci.model.command.CommandType;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class ShutdownCommand extends Command {
    public static final ShutdownCommand INSTANCE = new ShutdownCommand();

    private ShutdownCommand() {
    }

    @Override
    public void execute(Ci ci) {
        ci.shutdown();
    }

    @Override
    public void validate() {
        // do nothing
    }

    @Override
    public CommandType type() {
        return CommandType.SHUTDOWN;
    }
}
