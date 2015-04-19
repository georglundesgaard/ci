package no.lundesgaard.ci.command.show;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.TaskRunner;
import no.lundesgaard.ci.model.task.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Properties;

public class ShowTaskLogCommand extends ShowCommand {
	private static final Logger LOGGER = LoggerFactory.getLogger(ShowTaskLogCommand.class);

	private final String task;

	public ShowTaskLogCommand(Properties commandProperties) {
		this.task = commandProperties.getProperty("task");
	}

	@Override
	public void execute(Ci ci) {
		TaskStatus taskStatus = ci.taskStatuses().forTask(task)
				.stream()
				.sorted((status1, status2) -> status2.timestamp.compareTo(status1.timestamp))
				.findFirst()
				.orElse(null);
		Path outputLogPath = ci.workspace(taskStatus.taskRunnerId).resolve(TaskRunner.OUTPUT_LOG);
		try {
			String outputLog = readFile(outputLogPath.toFile());
			LOGGER.debug("Output log for task runner <{}>:\n{}", taskStatus.taskRunnerId, outputLog);
		} catch (IOException e) {
			LOGGER.warn("Failed to read output log <{}>", outputLogPath, e);
		}
	}

	private String readFile(File file) throws IOException {
		StringWriter stringWriter = new StringWriter();
		try (BufferedReader reader = new BufferedReader(new FileReader(file));
			 PrintWriter writer = new PrintWriter(stringWriter)) {
			String line;
			while ((line = reader.readLine()) != null) {
				writer.println(line);
			}
		}
		return stringWriter.toString();
	}

	@Override
	public void validate() {
		validateTask();
	}

	private void validateTask() {
		if (task == null) {
			throw new IllegalStateException("missing task");
		}
	}
}
