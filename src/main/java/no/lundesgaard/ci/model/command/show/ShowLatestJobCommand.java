package no.lundesgaard.ci.model.command.show;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.job.Job;
import no.lundesgaard.ci.model.task.Task;
import no.lundesgaard.ci.model.task.TaskId;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Properties;

import static no.lundesgaard.ci.model.task.TaskId.taskId;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class ShowLatestJobCommand extends ShowCommand {
	private static final Logger LOGGER = LoggerFactory.getLogger(ShowLatestJobCommand.class);

	private final String task;

	public ShowLatestJobCommand(Properties commandProperties) {
		this.task = commandProperties.getProperty("task");
	}

	@Override
	public void execute(Ci ci) {
		TaskId taskId = taskId(task);
		Job latestJob = ci.jobs().forTask(taskId)
				.stream()
				.sorted((job1, job2) -> job2.lastUpdated().compareTo(job1.lastUpdated()))
				.findFirst()
				.orElse(null);
		if (latestJob == null) {
			LOGGER.debug("No jobs for task <{}>", taskId);
		} else {
			Task task = ci.tasks().task(taskId);
			Path outputLogFile = ci.workspacesPath.resolve(latestJob.id).resolve(task.outputLogName());
			String outputLog = readFile(outputLogFile.toFile());
			LOGGER.debug("Output log for job <{}>:\n{}", latestJob.id, outputLog);
		}
	}

	private String readFile(File file) {
		StringWriter stringWriter = new StringWriter();
		try (BufferedReader reader = new BufferedReader(new FileReader(file));
			 PrintWriter writer = new PrintWriter(stringWriter)) {
			String line;
			while ((line = reader.readLine()) != null) {
				writer.println(line);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
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

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("task", task)
				.toString();
	}
}
