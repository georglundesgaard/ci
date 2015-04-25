package no.lundesgaard.ci.model.job;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class JobId implements Serializable {
	public final String id;

	public static JobId jobId(String id) {
		return new JobId(id);
	}

	public static JobId jobId(Job job) {
		return jobId(job.id);
	}

	private JobId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("id", id)
				.toString();
	}
}
