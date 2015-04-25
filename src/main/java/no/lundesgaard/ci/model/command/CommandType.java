package no.lundesgaard.ci.model.command;

import java.nio.file.Path;

public enum CommandType {
	SHUTDOWN,
	RESTART,
	CREATE,
	LIST,
	SHOW;

	public static CommandType from(Path commandPath) {
		String command = commandPath.getFileName().toString().toUpperCase();
		try {
			return valueOf(command);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unknown command <" + command + ">");
		}
	}
}
