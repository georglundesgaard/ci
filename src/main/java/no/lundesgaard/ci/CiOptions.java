package no.lundesgaard.ci;

import no.lundesgaard.ci.model.Type;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import static no.lundesgaard.ci.model.Type.SIMPLE;

public class CiOptions {
	private static final String ROOT = "root";
	private static final String TYPE = "type";
	private static final String HELP = "help";

	private final Options options;
	public final Type type;
	public final String root;

	public CiOptions(String[] args) {
		this.options = options();
		CommandLine commandLine = commandLine(options, args);
		if (commandLine == null || commandLine.hasOption(HELP)) {
			this.type = null;
			this.root = null;
		} else {
			this.type = type(commandLine);
			this.root = root(commandLine);
		}
	}

	private Options options() {
		Options options = new Options();
		options.addOption(rootOption());
		options.addOption(typeOption());
		options.addOption(helpOption());
		return options;
	}

	private Option rootOption() {
		Option rootOption = new Option("r", ROOT, true, "Root folder");
		rootOption.setRequired(true);
		return rootOption;
	}

	private Option typeOption() {
		return new Option("t", TYPE, true, "Type: simple or hazelcast");
	}

	private Option helpOption() {
		return new Option("h", HELP, false, "Print this message");
	}

	private CommandLine commandLine(Options options, String[] args)  {
		CommandLineParser commandLineParser = new BasicParser();
		try {
			return commandLineParser.parse(options, args);
		} catch (MissingOptionException e) {
			return null;
		} catch (ParseException e) {
			throw new RuntimeException("unexpected parse exception", e);
		}
	}

	private static Type type(CommandLine commandLine) {
		Type type;
		if (commandLine.hasOption(TYPE)) {
			type = Type.valueOf(commandLine.getOptionValue(TYPE).toUpperCase());
		} else {
			type = SIMPLE;
		}
		return type;
	}

	private static String root(CommandLine commandLine) {
		return commandLine.getOptionValue(ROOT);
	}

	public boolean isValid() {
		return type != null && root != null;
	}

	public void printHelp() {
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp("ci", options, true);
	}
}
