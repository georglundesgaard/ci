package no.lundesgaard.ci.model.trigger;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.event.Event;
import no.lundesgaard.ci.model.task.Task;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableSet;

public class TasksCompletedSuccessfullyTrigger implements Trigger {
	public final Set<String> tasks;

	public TasksCompletedSuccessfullyTrigger(String... tasks) {
		Set<String> taskSet = new HashSet<>();
		addAll(taskSet, tasks);
		this.tasks = unmodifiableSet(taskSet);
	}

	@Override
	public void onEvent(Ci ci, Task task, Event event) {
		// TODO
	}

	public boolean filter(Ci ci, Task parentTask, Event event) {
//		if (!(event instanceof TaskCompletedEvent)) {
//			return false;
//		}
//		TaskCompletedEvent taskCompletedEvent = (TaskCompletedEvent) event;
//		if (!tasks.contains(taskCompletedEvent.task)) {
//			return false;
//		}
//		if (tasks.size() == 1) {
//			return true;
//		}
		// TODO
		return true;
	}
}
