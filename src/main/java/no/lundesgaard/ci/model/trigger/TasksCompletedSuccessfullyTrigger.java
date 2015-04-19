package no.lundesgaard.ci.model.trigger;

import java.util.List;

import static java.util.Arrays.asList;

public class TasksCompletedSuccessfullyTrigger implements Trigger {
	public final List<String> tasks;

	public TasksCompletedSuccessfullyTrigger(String... tasks) {
		this.tasks = asList(tasks);
	}
}
