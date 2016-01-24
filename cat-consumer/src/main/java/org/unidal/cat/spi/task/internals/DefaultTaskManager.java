package org.unidal.cat.spi.task.internals;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.unidal.cat.spi.task.TaskConsumer;
import org.unidal.cat.spi.task.TaskManager;
import org.unidal.cat.spi.task.TaskPayload;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = TaskManager.class)
public class DefaultTaskManager implements TaskManager {
	@Inject
	private TaskRegistry m_registry;

	@Inject
	private TaskDispatcher m_dispatcher;

	@Inject
	private TaskQueue m_queue;

	private ExecutorService m_pool;

	@Override
	public void addTask(TaskPayload payload) {
		m_queue.add(payload);
	}

	@Override
	public void awaitTermination(int timeout, TimeUnit unit) {
		try {
			m_pool.awaitTermination(timeout, unit);
		} catch (InterruptedException e) {
			// ignore it
		}
	}

	@Override
	public void start() {
		m_dispatcher.setTaskManager(this);
		m_pool = Threads.forPool().getFixedThreadPool("Cat", 10);
		m_pool.submit(m_dispatcher);
	}

	@Override
	public void subscribe(String pattern, TaskConsumer consumer) {
		if (pattern.equals("*") || pattern.startsWith("*.") || pattern.endsWith(".*")) {
			m_registry.subscribePattern(pattern, consumer);
		} else {
			m_registry.subscribe(pattern, consumer);
		}
	}
}
