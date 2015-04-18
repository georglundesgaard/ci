package no.lundesgaard.ci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ListGitRepoCommand extends ListCommand {
	private final static Logger LOGGER = LoggerFactory.getLogger(ListGitRepoCommand.class);

	public ListGitRepoCommand() {
	}

	@Override
	public void execute(Ci ci) {
		LOGGER.debug("{} repositories", ci.repositories().count());
		ci.repositories()
				.stream()
				.forEach(repository -> {
					LOGGER.debug("{}", repository);
				});
	}

	@Override
	public void validate() {
		// nothing to validate
	}
}
