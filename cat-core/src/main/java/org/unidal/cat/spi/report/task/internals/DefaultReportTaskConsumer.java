package org.unidal.cat.spi.report.task.internals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unidal.cat.spi.report.task.ReportTask;
import org.unidal.cat.spi.report.task.ReportTaskConsumer;
import org.unidal.cat.spi.report.task.ReportTaskExecutor;
import org.unidal.cat.spi.report.task.ReportTaskService;
import org.unidal.helper.Inets;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = ReportTaskConsumer.class)
public class DefaultReportTaskConsumer implements ReportTaskConsumer {
	@Inject
	private ReportTaskService m_service;

	@Inject
	private ReportTaskExecutor m_executor;

	private AtomicBoolean m_active = new AtomicBoolean(true);

	private CountDownLatch m_latch = new CountDownLatch(1);

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public void run() {
		String ip = Inets.IP4.getLocalHostAddress();

		try {
			while (m_active.get()) {
				try {
					ReportTask task = m_service.pull(ip);

					if (task != null) {
						String message = null;

						try {
							m_executor.execute(task);
						} catch (Throwable e) {
							message = e.toString();
							Cat.logError(e);
						}

						if (message == null) {
							m_service.complete(task);
						} else {
							m_service.fail(task, message);
						}
					} else {
					}
				} catch (Throwable e) {
					Cat.logError(e);
				}

				TimeUnit.SECONDS.sleep(5);
			}
		} catch (InterruptedException e) {
			// ignore it
		} finally {
			m_latch.countDown();
		}
	}

	@Override
	public void shutdown() {
		m_active.set(false);

		try {
			m_latch.await(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// ignore it
		}
	}
}
