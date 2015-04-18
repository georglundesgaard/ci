package no.lundesgaard.ci.data;

import no.lundesgaard.ci.Ci;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

import static java.time.Instant.now;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

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

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("name", name)
				.append("url", url)
				.append("nodeId", nodeId)
				.append("scanInterval", scanInterval)
				.append("lastScan", lastScan)
				.toString();
	}
}
