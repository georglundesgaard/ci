package no.lundesgaard.ci.model.event;

import no.lundesgaard.ci.Ci;

public interface Event {
	void process(Ci ci);
}
