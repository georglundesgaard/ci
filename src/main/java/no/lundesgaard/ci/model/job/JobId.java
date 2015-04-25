package no.lundesgaard.ci.model.job;

import java.io.Serializable;

public class JobId implements Serializable {
	public final String id;

	public JobId(String id) {
		this.id = id;
	}
}
