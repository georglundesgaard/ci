package no.lundesgaard.ci.repositories.resource;

import java.time.ZonedDateTime;

import org.springframework.hateoas.ResourceSupport;

public class CommitResource extends ResourceSupport {
	private String commitId;
	private AuthorResource author;
	private ZonedDateTime commitDateTime;
	private String fullMessage;

	public CommitResource(String commitId, AuthorResource author, ZonedDateTime commitDateTime, String fullMessage) {
		this.commitId = commitId;
		this.author = author;
		this.commitDateTime = commitDateTime;
		this.fullMessage = fullMessage;
	}

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public AuthorResource getAuthor() {
		return author;
	}

	public void setAuthor(AuthorResource author) {
		this.author = author;
	}

	public ZonedDateTime getCommitDateTime() {
		return commitDateTime;
	}

	public void setCommitDateTime(ZonedDateTime commitDateTime) {
		this.commitDateTime = commitDateTime;
	}

	public String getFullMessage() {
		return fullMessage;
	}

	public void setFullMessage(String fullMessage) {
		this.fullMessage = fullMessage;
	}
}
