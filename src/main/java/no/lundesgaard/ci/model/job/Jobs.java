package no.lundesgaard.ci.model.job;

import java.util.Map;
import java.util.stream.Stream;

public class Jobs {
	private final Map<String, Job> jobMap;

	public Jobs(Map<String, Job> jobMap) {
		this.jobMap = jobMap;
	}

	public Job job(Job job) {
		jobMap.put(job.id, job);
		return job;
	}

	public Job job(String id) {
		return jobMap.get(id);
	}

	public Stream<Job> stream() {
		return jobMap.values().stream();
	}
}
