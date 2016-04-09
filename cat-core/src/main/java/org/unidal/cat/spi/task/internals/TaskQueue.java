package org.unidal.cat.spi.task.internals;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.unidal.cat.spi.task.TaskPayload;
import org.unidal.lookup.annotation.Named;

@Named
public class TaskQueue {
	private BlockingQueue<TaskPayload> m_queue = new LinkedBlockingQueue<TaskPayload>();

	public void add(TaskPayload payload) {
		m_queue.add(payload);
	}

	public TaskPayload poll(int timeout, TimeUnit unit) throws InterruptedException {
		return m_queue.poll(timeout, unit);
	}
}
