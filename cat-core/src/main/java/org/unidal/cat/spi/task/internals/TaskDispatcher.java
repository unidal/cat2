package org.unidal.cat.spi.task.internals;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unidal.cat.spi.task.TaskConsumer;
import org.unidal.cat.spi.task.TaskManager;
import org.unidal.cat.spi.task.TaskPayload;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named
public class TaskDispatcher implements Task {
	@Inject
	private TaskRegistry m_registry;

	@Inject
	private TaskQueue m_queue;

	private AtomicBoolean m_enabled = new AtomicBoolean(true);

	private TaskManager m_manager;

	private void dispatch(final TaskPayload payload) {
		Set<TaskConsumer> consumers = m_registry.getConsumers(payload.getSubject());

		for (final TaskConsumer consumer : consumers) {
			consumer.onTask(m_manager, payload);
		}
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public void run() {
		try {
			while (m_enabled.get()) {
				TaskPayload payload = m_queue.poll(5, TimeUnit.MILLISECONDS);

				if (payload != null) {
					try {
						dispatch(payload);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (InterruptedException e) {
			// ignore it
		}
	}

	public void setTaskManager(TaskManager manager) {
		m_manager = manager;
	}

	@Override
	public void shutdown() {
		m_enabled.set(false);
	}
}
