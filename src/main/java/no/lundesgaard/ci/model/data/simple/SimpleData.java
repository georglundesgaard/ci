package no.lundesgaard.ci.model.data.simple;

import no.lundesgaard.ci.model.data.Data;
import no.lundesgaard.ci.model.job.JobQueue;
import no.lundesgaard.ci.model.job.Jobs;
import no.lundesgaard.ci.model.repository.Repositories;
import no.lundesgaard.ci.model.task.Tasks;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleData implements Data {
	private final Repositories repositories = new Repositories(new HashMap<>());
	private final Tasks tasks = new Tasks(new HashMap<>());
	private final Jobs jobs = new Jobs(new HashMap<>());
	private final JobQueue jobQueue = new JobQueue(new LinkedBlockingQueue<>());

	@Override
	public String nodeId() {
		return "simple";
	}

	@Override
	public Repositories repositories() {
		return repositories;
	}

	@Override
	public Tasks tasks() {
		return tasks;
	}

	@Override
	public Jobs jobs() {
		return jobs;
	}

	@Override
	public JobQueue jobQueue() {
		return jobQueue;
	}

	@Override
	public void shutdown() {
		// do nothing
	}
}
