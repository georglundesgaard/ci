package no.lundesgaard.ci.model.task;

import no.lundesgaard.ci.model.trigger.Trigger;
import no.lundesgaard.ci.model.workspace.Workspace;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class Task implements Serializable {
	public final String name;
	public final Trigger trigger;
	public final Workspace workspace;
	public final String script;

	public Task(String name, Trigger trigger, Workspace workspace, String script) {
		this.name = name;
		this.trigger = trigger;
		this.workspace = workspace;
		this.script = script;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("name", name)
				.append("trigger", trigger)
				.append("workspace", workspace)
				.toString();
	}
}
