package no.lundesgaard.ci.data;

import no.lundesgaard.ci.Ci;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

import static java.time.Instant.now;

public abstract class Repository implements Serializable {
	public final String name;
	public final String url;
	public final String nodeId;
	public final Duration scanInterval = Duration.ofMinutes(1);
	public final Instant lastScan;

	public Repository(String name, String url, String nodeId) {
		this.name = name;
		this.url = url;
		this.nodeId = nodeId;
		this.lastScan = null;
	}

	public Repository(Repository repository, Instant lastScan) {
		this.name = repository.name;
		this.url = repository.url;
		this.nodeId = repository.nodeId;
		this.lastScan = lastScan;
	}

	public boolean readyForScan() {
		return lastScan == null || now().isAfter(lastScan.plus(scanInterval));
	}

	public abstract Repository scan(Ci ci);
}
