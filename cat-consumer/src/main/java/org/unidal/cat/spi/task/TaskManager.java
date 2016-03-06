package org.unidal.cat.spi.task;

import java.util.concurrent.TimeUnit;

public interface TaskManager {

	public void addTask(TaskPayload payload);

	public void awaitTermination(int timeout, TimeUnit unit);

	public void start();

	public void subscribe(String topicPattern, TaskConsumer consumer);
}
