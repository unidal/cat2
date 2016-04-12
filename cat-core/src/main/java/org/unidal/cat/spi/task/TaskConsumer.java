package org.unidal.cat.spi.task;

public interface TaskConsumer {
	public TaskStatus onTask(TaskManager manager, TaskPayload payload);
}
