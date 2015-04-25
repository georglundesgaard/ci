package no.lundesgaard.ci.model.command.create;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.repository.GitRepository;
import no.lundesgaard.ci.model.repository.Repository;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Properties;

public class CreateGitRepoCommand extends CreateCommand {
	private final String name;
	private final String url;

	public CreateGitRepoCommand(Properties commandProperties) {
		this.name = commandProperties.getProperty("name");
		this.url = commandProperties.getProperty("url");
	}

	@Override
	public void execute(Ci ci) {
		Repository repository = new GitRepository(name, url, ci.nodeId());
		ci.repositories().repository(repository);
	}

	@Override
	public void validate() {
		validateName();
		validateUrl();
	}

	private void validateName() {
		if (name == null) {
			throw new IllegalStateException("missing name");
		}
	}

	private void validateUrl() {
		if (url == null) {
			throw new IllegalStateException("mssing url");
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.appendSuper(super.toString())
				.append("name", name)
				.append("url", url)
				.toString();
	}
}
