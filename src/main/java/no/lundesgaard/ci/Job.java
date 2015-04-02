package no.lundesgaard.ci;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;

public class Job implements Serializable {
	private final String id;
	private final String name;
	private final String script;

	public Job(String id, String name, String script) {
		this.id = id;
		this.name = name;
		this.script = script;
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

	public void run(File workspaces) throws Exception {
		File workspace = new File(workspaces, id);
		workspace.mkdir();
		File scriptFile = new File(workspace, name);
		FileWriter fileWriter = new FileWriter(scriptFile);
		fileWriter.write(script);
		fileWriter.close();
		Process process = new ProcessBuilder("sh", name)
				.directory(workspace)
				.redirectOutput(new File(workspace, "output.log"))
				.redirectError(new File(workspace, "error.log"))
				.start();
		process.waitFor();
	}
}
