package no.lundesgaard.ci.repositories.mapper;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import no.lundesgaard.ci.repositories.model.Author;
import no.lundesgaard.ci.repositories.model.Commit;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Component;

@Component
public class RevCommitToCommitMapper {
	public Commit commit(RevCommit revCommit) {
		String commitId = revCommit.getName();
		PersonIdent authorIdent = revCommit.getAuthorIdent();
		Author author = new Author(authorIdent.getName(), authorIdent.getEmailAddress());
		int commitTime = revCommit.getCommitTime();
		String fullMessage = revCommit.getFullMessage();
		ZonedDateTime commitDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(commitTime), ZoneId.systemDefault());
		return new Commit(commitId, author, commitDateTime, fullMessage);
	}
}
