package no.lundesgaard.ci.model.event;

import no.lundesgaard.ci.model.job.JobId;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class JobCompletedEvent extends Event {
	public final JobId jobId;

	public JobCompletedEvent(JobId jobId) {
		this.jobId = jobId;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("jobId", jobId)
				.toString();
	}
}
