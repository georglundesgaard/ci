package no.lundesgaard.ci.repositories.service;

public class GitServiceException extends RuntimeException {
	public GitServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
