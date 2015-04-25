package no.lundesgaard.ci.model.event;

import no.lundesgaard.ci.Ci;

// TODO
public class TaskCompletedEvent implements Event {
//	public final Task task;
//	public final TaskStatus taskStatus;

//	public TaskCompletedEvent(Task task, TaskStatus taskStatus) {
//		this.task = task;
//		this.taskStatus = taskStatus;
//	}

	@Override
	public void process(Ci ci) {
//		ci.tasks().stream()
//				.filter(this::taskFilter)
//				.map(task -> task.name)
//				.forEach(ci::addTaskToQueue);
	}

//	private boolean taskFilter(Task task) {
//		if (!(task.trigger instanceof TasksCompletedSuccessfullyTrigger)) {
//			return false;
//		}
//		TasksCompletedSuccessfullyTrigger tasksCompletedSuccessfullyTrigger =
//				(TasksCompletedSuccessfullyTrigger) task.trigger;
//	}

//	@Override
//	public String toString() {
//		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
//				.append("task", task)
//				.toString();
//	}
}
