package no.lundesgaard.ci.model.data.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import no.lundesgaard.ci.model.data.Data;
import no.lundesgaard.ci.model.job.JobQueue;
import no.lundesgaard.ci.model.job.Jobs;
import no.lundesgaard.ci.model.repository.Repositories;
import no.lundesgaard.ci.model.task.Tasks;

public class HazelcastData implements Data {
	private final HazelcastInstance hazelcastInstance;

	public HazelcastData(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	@Override
	public String nodeId() {
		return hazelcastInstance.getLocalEndpoint().getUuid();
	}

	@Override
	public Repositories repositories() {
		return new Repositories(hazelcastInstance.getMap("repositoryMap"));
	}

	@Override
	public Tasks tasks() {
		return new Tasks(hazelcastInstance.getMap("taskMap"));
	}

	@Override
	public Jobs jobs() {
		return new Jobs(hazelcastInstance.getMap("jobMap"));
	}

	@Override
	public JobQueue jobQueue() {
		return new JobQueue(hazelcastInstance.getQueue("jobQueue"));
	}

	@Override
	public void shutdown() {
		hazelcastInstance.shutdown();
	}
}
