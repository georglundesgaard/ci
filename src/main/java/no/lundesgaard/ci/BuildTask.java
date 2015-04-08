package no.lundesgaard.ci;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

import static java.lang.String.format;
import static java.nio.file.Files.move;

public class BuildTask extends Task {
	private final static Logger LOGGER = LoggerFactory.getLogger(BuildTask.class);

	private final String name;
	private final String repoName;
	private final String repoType;
	private final String buildCommand;
	private String lastCommitId;

	public BuildTask(Properties buildTaskProperties) {
		super(buildTaskProperties);
		this.name = buildTaskProperties.getProperty("name");
		this.repoName = buildTaskProperties.getProperty("repo-name");
		this.repoType = buildTaskProperties.getProperty("repo-type");
		this.buildCommand = buildTaskProperties.getProperty("build-command");
	}

	public static Task from(Path buildTaskPath) throws IOException {
		Properties buildTaskProperties = new Properties();
		try (FileReader buildTaskReader = new FileReader(buildTaskPath.toFile())) {
			buildTaskProperties.load(buildTaskReader);
		}
		return new BuildTask(buildTaskProperties);
	}

	@Override
	public void execute(Ci ci, String taskId) {
		super.execute(ci, taskId);
		Path jobsPath = ci.getJobsPath();
		Path tempJobPath = jobsPath.resolve(UUID.randomUUID().toString());
		try (BufferedReader scriptReader = scriptReader(); PrintWriter scriptWriter = scriptWriter(tempJobPath)) {
			scriptReader
					.lines()
					.map(line -> formatLine(ci, taskId, line))
					.forEach(scriptWriter::println);
			scriptWriter.flush();
			Path jobPath = jobsPath.resolve(taskId + ".sh");
			move(tempJobPath, jobPath);
		} catch (IOException e) {
			LOGGER.error("Failed to create build script: {}", e.getMessage(), e);
		}
	}

	private String formatLine(Ci ci, String taskId, String line) {
		if ("cp -a %s %s".equals(line)) {
			Path repositoriesPath = ci.getRepositoriesPath();
			Path repositoryPath = repositoriesPath.resolve(repoName);
			return format(line, repositoryPath.toString(), repoName);
		}
		if ("cd %s".equals(line)) {
			return format(line, repoName);
		}
		if ("if [ \"$commit_id\" != \"%s\" ]; then".equals(line)) {
			return format(line, lastCommitId != null ? lastCommitId : "");
		}
		if ("  %s".equals(line)) {
			return format(line, buildCommand);
		}
		if ("  echo \"last-commit-id=$commit_id\" > %s".equals(line)) {
			return format(line, taskId);
		}
		return line;
	}

	private BufferedReader scriptReader() throws IOException{
		return new BufferedReader(
				new InputStreamReader(
						getClass().getResourceAsStream("/scripts/build.sh"),
						StandardCharsets.UTF_8
				)
		);
	}

	private PrintWriter scriptWriter(Path scriptPath) throws IOException {
		return new PrintWriter(new FileWriter(scriptPath.toFile()));
	}

	@Override
	public void stop(Properties taskProperties) {
		super.stop(taskProperties);
		this.lastCommitId = taskProperties.getProperty("last-commit-id");
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("name", name)
				.toString();
	}
}
