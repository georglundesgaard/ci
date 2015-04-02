package no.lundesgaard.ci;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;

public class Job implements Serializable {
	private final static Logger LOGGER = LoggerFactory.getLogger(Ci.class);

	private final String id;
	private final String name;
	private final String script;
	private Process process;

	public Job(String id, String name, String script) {
		this.id = id;
		this.name = name;
		this.script = script;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		Job job = (Job) o;

		return new EqualsBuilder()
				.append(id, job.id)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(id)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", id)
				.append("name", name)
				.toString();
	}

	public void run(File workspaces) throws Exception {
		File workspace = new File(workspaces, id);
		workspace.mkdir();
		File scriptFile = new File(workspace, name);
		FileWriter fileWriter = new FileWriter(scriptFile);
		fileWriter.write(script);
		fileWriter.close();
		File outputLog = new File(workspace, "output.log");
		process = new ProcessBuilder("sh", name)
				.directory(workspace)
				.redirectOutput(ProcessBuilder.Redirect.appendTo(outputLog))
				.redirectError(ProcessBuilder.Redirect.appendTo(outputLog))
				.start();
		LOGGER.debug("job with id <{}> and name <{}> started...", id, name);
		new Thread(() -> {
			try {
				int exitCode = process.waitFor();
				LOGGER.debug("job with id <{}> and name <{}> exited with code: {}", id, name, exitCode);
			} catch (InterruptedException e) {
				LOGGER.warn("Waiting for process <{}> was interrupted.", process, e);
			}
		}).start();
	}

	public boolean isRunning() {
		return process.isAlive();
	}

	public void stop() throws Exception {
		Process process = this.process.destroyForcibly();
		LOGGER.debug("job with id <{}> and name <{}> stopped. Exit code: {}", id, name, process.waitFor());
	}
}
