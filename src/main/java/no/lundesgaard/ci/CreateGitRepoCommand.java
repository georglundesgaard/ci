package no.lundesgaard.ci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

import static java.lang.String.format;
import static java.nio.file.Files.move;

public class CreateGitRepoCommand extends CreateCommand {
	private final static Logger LOGGER = LoggerFactory.getLogger(CreateGitRepoCommand.class);

	private final String url;
	private final String name;

	public CreateGitRepoCommand(Properties commandProperties) {
		this.url = commandProperties.getProperty("url");
		this.name = commandProperties.getProperty("name");
	}

	@Override
	public void execute(Ci ci) {
		Path jobsPath = ci.getJobsPath();
		Path tempJobPath = jobsPath.resolve(UUID.randomUUID().toString());
		try (BufferedReader scriptReader = scriptReader(); PrintWriter scriptWriter = scriptWriter(tempJobPath)) {
			scriptReader
					.lines()
					.map(line -> formatLine(ci, line))
					.forEach(scriptWriter::println);
			scriptWriter.flush();
			Path jobPath = jobsPath.resolve("git-clone-" + UUID.randomUUID().toString() + ".sh");
			move(tempJobPath, jobPath);
		} catch (IOException e) {
			LOGGER.error("Failed to create git clone script: {}", e.getMessage(), e);
		}
	}

	private String formatLine(Ci ci, String line) {
		if ("cd %s".equals(line)) {
			Path repositoriesPath = ci.getRepositoriesPath();
			return format(line, repositoriesPath.toString());
		}
		if ("git clone %s %s".equals(line)) {
			return format(line, url, name);
		}
		return line;
	}

	private BufferedReader scriptReader() throws IOException{
		return new BufferedReader(
				new InputStreamReader(
						getClass().getResourceAsStream("/scripts/git-clone.sh"),
						StandardCharsets.UTF_8
				)
		);
	}

	private PrintWriter scriptWriter(Path scriptPath) throws IOException{
		return new PrintWriter(new FileWriter(scriptPath.toFile()));
	}

	@Override
	public void validate() {
		validateUrl();
		validateName();
	}

	private void validateUrl() {
		if (url == null) {
			throw new IllegalStateException("mssing url");
		}
	}

	private void validateName() {
		if (name == null) {
			throw new IllegalStateException("missing name");
		}
	}
}
