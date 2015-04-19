package no.lundesgaard.ci.model.command;

import no.lundesgaard.ci.Ci;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListGitRepoCommand extends ListCommand {
	private final static Logger LOGGER = LoggerFactory.getLogger(ListGitRepoCommand.class);
	public final static ListGitRepoCommand INSTANCE = new ListGitRepoCommand();

	private ListGitRepoCommand() {
	}

	@Override
	public void execute(Ci ci) {
		int count = ci.repositories().count();
		LOGGER.debug("{} {}", count, count == 1 ? "repository" : "repositories");
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
