package no.lundesgaard.ci.model.task;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.trigger.Trigger;
import no.lundesgaard.ci.model.workspace.Workspace;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class Task implements Serializable {
	public final String name;
	public final Trigger trigger;
	public final Workspace workspace;
	public final String script;

	public Task(String name, Trigger trigger, Workspace workspace, String script) {
		this.name = name;
		this.trigger = trigger;
		this.workspace = workspace;
		this.script = script;
	}

	public Path initWorkspace(Ci ci, String workspaceName) {
		Path workspacePath = workspace.init(ci, workspaceName);
		writeScriptFile(workspacePath);
		return workspacePath;
	}

	private void writeScriptFile(Path workspacePath) {
		Path scriptPath = workspacePath.resolve(scriptName());
		try (FileWriter fileWriter = new FileWriter(scriptPath.toFile())) {
			fileWriter.write(script);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private String scriptName() {
		return commonName() + ".sh";
	}

	private String commonName() {
		return "task-" + name;
	}

	public Process startProcess(Path workspacePath) {
		File outputLogFile = workspacePath.resolve(outputLogName()).toFile();
		try {
			return new ProcessBuilder("sh", scriptName())
					.directory(workspacePath.toFile())
					.redirectOutput(ProcessBuilder.Redirect.appendTo(outputLogFile))
					.redirectError(ProcessBuilder.Redirect.appendTo(outputLogFile))
					.start();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private String outputLogName() {
		return commonName() + ".log";
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("name", name)
				.append("trigger", trigger)
				.append("workspace", workspace)
				.toString();
	}
}
