package no.lundesgaard.ci.repositories.model;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class RepositoryUpdateEvent implements Serializable {
	private String value;

	public RepositoryUpdateEvent(String value) {
		this.value = value;
	}

	public String getType() {
		return "repository-update";
	}

	public String getValue() {
		return value;
	}
}
