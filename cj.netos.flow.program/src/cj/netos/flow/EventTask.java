package cj.netos.flow;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EventTask {
    String taskName;
    long interval = 0;
    Map<String, Object> parameters;

    public EventTask() {
        parameters = new HashMap<>();
    }

    public EventTask(String taskName,long interval) {
        this();
        this.taskName = taskName;
        this.interval=interval;
    }

    public long interval() {
        return interval;
    }

    public void interval(long interval) {
        this.interval = interval;
    }

    public String taskName() {
        return taskName;
    }

    public void taskName(String taskName) {
        this.taskName = taskName;
    }

    public Object parameter(String key) {
        return parameters.get(key);
    }

    public void parameter(String key, Object value) {
        parameters.put(key, value);
    }

    public Set<String> keys() {
        return parameters.keySet();
    }

    @Override
    public String toString() {
        return "EventTask{" +
                "taskName='" + taskName + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
