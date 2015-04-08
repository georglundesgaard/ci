package no.lundesgaard.ci;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

public class Job implements Serializable {
    private final String id;
    private final String name;
    private final String script;
    private final String taskId;

    public Job(String id, String name, String script, String taskId) {
        this.id = id;
        this.name = name;
        this.script = script;
        this.taskId = taskId;
    }

    public Job(String id, String name, String script) {
        this.id = id;
        this.name = name;
        this.script = script;
        this.taskId = null;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getScript() {
        return script;
    }

    public String getTaskId() {
        return taskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Job job = (Job) o;

        return new EqualsBuilder()
                .append(id, job.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("name", name)
                .append("taskId", taskId)
                .toString();
    }
}
