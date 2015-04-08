package no.lundesgaard.ci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

import static java.lang.String.format;
import static java.nio.file.Files.move;

public class CreateBuildTaskCommand extends CreateTaskCommand {
	private final static Logger LOGGER = LoggerFactory.getLogger(CreateBuildTaskCommand.class);

	private final String name;
	private final String repoName;
	private final String repoType;
	private final String buildCommand;

	public CreateBuildTaskCommand(Properties commandProperties) {
		super(commandProperties);
		this.name = commandProperties.getProperty("name");
		this.repoName = commandProperties.getProperty("repo-name");
		this.repoType = commandProperties.getProperty("repo-type");
		this.buildCommand =  commandProperties.getProperty("build-command");
	}

	@Override
	public void execute(Ci ci) {
		Path tasksPath = ci.getTasksPath();
		String buildId = UUID.randomUUID().toString();
		Path tempTaskPath = tasksPath.resolve("temp-" + buildId);
		try (PrintWriter taskWriter = taskWriter(tempTaskPath)) {
			taskWriter.println(format("name=%s", name));
			taskWriter.println(format("trigger=%s", trigger));
			taskWriter.println(format("repo-name=%s", repoName));
			taskWriter.println(format("repo-type=%s", repoType));
			taskWriter.println(format("build-command=%s", buildCommand));
			Path taskPath = tasksPath.resolve("build-" + buildId);
			move(tempTaskPath, taskPath);
		} catch (IOException e) {
			LOGGER.error("Failed to create task: {}", e.getMessage(), e);
		}
	}

	private PrintWriter taskWriter(Path tempTaskPath) throws IOException {
		return new PrintWriter(new FileWriter(tempTaskPath.toFile()));
	}

	@Override
	public void validate() {
		validateName();
		validateRepoName();
		validateRepoType();
		validateBuildCommand();
	}

	private void validateName() {
		validateNull("name", name);
	}

	private void validateRepoName() {
		validateNull("repo-name", repoName);
	}

	private void validateRepoType() {
		validateNull("repo-type", repoType);
	}

	private void validateBuildCommand() {
		validateNull("build-command", buildCommand);
	}

	private void validateNull(String name, String value) {
		if (value == null) {
			throw new IllegalStateException("missing " + name);
		}
	}
}
