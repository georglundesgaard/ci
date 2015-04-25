package no.lundesgaard.ci.processor;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.lundesgaard.ci.processor.Processor.State.CREATED;
import static no.lundesgaard.ci.processor.Processor.State.RUNNING;
import static no.lundesgaard.ci.processor.Processor.State.STOPPED;

public class CommandProcessor extends Processor {
	public static final Logger LOGGER = LoggerFactory.getLogger(CommandProcessor.class);

	public CommandProcessor(Ci ci) {
		super(ci);
	}

	@Override
	public void run() {
		init();
		try {
			while (state == RUNNING) {
				tryNextCommand();
				sleep();
			}
		} finally {
			state = STOPPED;
			LOGGER.debug("Command processor stopped");
		}
	}

	private void init() {
		if (state != CREATED) {
			throw new IllegalStateException("Command processor is already running");
		}
		LOGGER.debug("Command processor started");
		state = RUNNING;
	}

	private void tryNextCommand() {
		Command command = Command.nextFrom(ci.commandsPath);
		if (command == null) {
			return;
		}
		try {
			command.validate();
			LOGGER.debug("Command accepted: {}", command);
			command.execute(ci);
		} catch (IllegalStateException e) {
			LOGGER.warn("Invalid command: {}", command, e);
		}
	}
}
