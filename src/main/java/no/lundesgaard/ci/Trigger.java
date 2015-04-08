package no.lundesgaard.ci;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.MINUTES;

public enum Trigger {
	EVERY_MINUTE {
		@Override
		public boolean isExpired(Instant lastExecuted) {
			return lastExecuted == null || lastExecuted.plus(1, MINUTES).isBefore(Instant.now());
		}
	};

	public abstract boolean isExpired(Instant lastTriggered);
}
