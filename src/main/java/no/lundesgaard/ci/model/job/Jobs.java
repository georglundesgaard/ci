package no.lundesgaard.ci.model.job;

import no.lundesgaard.ci.model.task.TaskId;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Jobs {
	private final Map<String, Job> jobMap;

	public Jobs(Map<String, Job> jobMap) {
		this.jobMap = jobMap;
	}

	public Job job(Job job) {
		jobMap.put(job.id, job);
		return job;
	}

	public Job job(JobId jobId) {
		return jobMap.get(jobId.id);
	}

	public Stream<Job> stream() {
		return jobMap.values().stream();
	}

	public List<Job> forTask(TaskId taskId) {
		return jobMap
				.values()
				.stream()
				.filter(job -> job.taskId.equals(taskId))
				.collect(toList());
	}
}
