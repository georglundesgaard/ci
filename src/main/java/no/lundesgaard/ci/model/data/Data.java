package no.lundesgaard.ci.model.data;

import no.lundesgaard.ci.model.job.JobQueue;
import no.lundesgaard.ci.model.job.Jobs;
import no.lundesgaard.ci.model.repository.Repositories;
import no.lundesgaard.ci.model.task.Tasks;

public interface Data {
	Repositories repositories();
	Tasks tasks();
	Jobs jobs();
	JobQueue jobQueue();
	void shutdown();
}
