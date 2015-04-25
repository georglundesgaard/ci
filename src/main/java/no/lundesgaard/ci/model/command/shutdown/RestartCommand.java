package no.lundesgaard.ci.model.command.shutdown;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.command.Command;
import no.lundesgaard.ci.model.command.CommandType;

public class RestartCommand extends Command {
	public static final RestartCommand INSTANCE = new RestartCommand();

	private RestartCommand() {
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
		return CommandType.RESTART;
	}
}
