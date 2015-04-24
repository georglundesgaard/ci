package no.lundesgaard.ci.task;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

public class TaskQueueItem {
	public final String taskName;
	public final Map<String, String> properties;

	public TaskQueueItem(String taskName, Map<String, String> properties) {
		this.taskName = taskName;
		this.properties = unmodifiableMap(new HashMap<>(properties));
	}
}
