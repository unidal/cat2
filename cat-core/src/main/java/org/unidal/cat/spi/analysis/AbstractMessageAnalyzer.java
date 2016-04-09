package org.unidal.cat.spi.analysis;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.ReportManagerManager;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.message.io.DefaultMessageQueue;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;

public abstract class AbstractMessageAnalyzer<R extends Report> implements MessageAnalyzer {
	@Inject
	private ReportManagerManager m_reportManagerManager;

	private String m_name;

	private int m_hour;

	private int m_index;

	private String[] m_dependencies;

	private int m_errors;

	private ReportManager<R> m_reportManager;

	private MessageQueue m_queue;

	private CountDownLatch m_latch = new CountDownLatch(1);

	private AtomicBoolean m_enabled = new AtomicBoolean(true);

	public AbstractMessageAnalyzer(String name, String... dependencies) {
		m_name = name;
		m_dependencies = dependencies;
		m_queue = new DefaultMessageQueue(getQueueSize());
	}

	@Override
	public void doCheckpoint(boolean atEnd) throws IOException {
		shutdown();

		m_reportManager.doCheckpoint(new Date(TimeUnit.HOURS.toMillis(m_hour)), m_index, atEnd);
	}

	@Override
	public String[] getDependencies() {
		return m_dependencies;
	}

	protected R getLocalReport(String domain) {
		return m_reportManager.getLocalReport(domain, null, m_index, true);
	}

	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public MessageQueue getQueue() {
		return m_queue;
	}

	protected int getQueueSize() {
		return 30000;
	}

	protected ReportManager<R> getReportManager() {
		return m_reportManager;
	}

	protected void handleException(Throwable e) {
		m_errors++;

		// sampling logging
		if (m_errors == 1 || m_errors % 10000 == 0) {
			Cat.logError(e);
		}
	}

	@Override
	public void initialize(int index, int hour) throws IOException {
		m_index = index;
		m_hour = hour;
		m_reportManager = m_reportManagerManager.getReportManager(m_name);
		m_reportManager.doInitLoad(new Date(TimeUnit.HOURS.toMillis(m_hour)), m_index);
	}

	protected abstract void process(MessageTree tree);

	@Override
	public void run() {
		while (m_enabled.get() || m_queue.size() > 0) {
			MessageTree tree = m_queue.poll();

			if (tree != null) {
				try {
					process(tree);
				} catch (Throwable e) {
					handleException(e);
				}
			}
		}

		m_latch.countDown();
	}

	@Override
	public void shutdown() {
		int timeout = 10; // 10 seconds

		m_enabled.set(false);

		// wait for run() to end
		try {
			m_latch.await(timeout, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// ignore it
			String msg = String.format("[WARN] Analyzer(%s-%s) did not finish checkout in %s seconds!", m_name, m_index,
			      timeout);

			System.err.println(msg);
		}
	}

	@Override
	public String toString() {
		return String.format("%s(%s-%s-%s)", getClass().getSimpleName(), m_name, m_hour, m_index);
	}
}
