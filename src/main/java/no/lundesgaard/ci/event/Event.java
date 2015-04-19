package no.lundesgaard.ci.event;

import no.lundesgaard.ci.Ci;

public interface Event {
	void process(Ci ci);
}
