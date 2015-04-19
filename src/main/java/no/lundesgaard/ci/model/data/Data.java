package no.lundesgaard.ci.model.data;

import no.lundesgaard.ci.model.repository.Repositories;
import no.lundesgaard.ci.model.task.TaskQueue;
import no.lundesgaard.ci.model.task.TaskStatuses;
import no.lundesgaard.ci.model.task.Tasks;

public interface Data {
	Repositories repositories();
	Tasks tasks();
	TaskQueue taskQueue();
	TaskStatuses taskStatuses();
	void shutdown();
}
