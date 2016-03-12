package no.lundesgaard.ci.repositories.model;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Commit {
	private final String commitId;
	private final Author author;
	private final ZonedDateTime commitDateTime;
	private final String fullMessage;

	public Commit(String commitId, Author author, ZonedDateTime commitDateTime, String fullMessage) {
		this.commitId = commitId;
		this.author = author;
		this.commitDateTime = commitDateTime;
		this.fullMessage = fullMessage;
	}

	public String getCommitId() {
		return commitId;
	}

	public Author getAuthor() {
		return author;
	}

	public String getFullMessage() {
		return fullMessage;
	}

	public ZonedDateTime getCommitDateTime() {
		return commitDateTime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Commit commit = (Commit) o;
		return new EqualsBuilder()
				.append(commitId, commit.commitId)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(commitId)
				.toHashCode();
	}
}
