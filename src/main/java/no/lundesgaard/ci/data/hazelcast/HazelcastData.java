package no.lundesgaard.ci.data.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import no.lundesgaard.ci.data.Data;
import no.lundesgaard.ci.data.Repositories;

public class HazelcastData implements Data {
	private final HazelcastInstance hazelcastInstance;

	public HazelcastData(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	@Override
	public Repositories repositories() {
		return new Repositories(hazelcastInstance.getMap("repositoryMap"));
	}
}
