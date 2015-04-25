package no.lundesgaard.ci.model.trigger;

import no.lundesgaard.ci.Ci;
import no.lundesgaard.ci.model.event.Event;
import no.lundesgaard.ci.model.task.Task;

import java.io.Serializable;

public interface Trigger extends Serializable {
	void onEvent(Ci ci, Task task, Event event);
}
