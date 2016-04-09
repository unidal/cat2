package org.unidal.cat.spi.analysis;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportManager;
import org.unidal.cat.spi.ReportManagerManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.extension.RoleHintEnabled;

import com.dianping.cat.Cat;
import com.dianping.cat.message.io.DefaultMessageQueue;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;

public abstract class AbstractMessageAnalyzer<R extends Report> extends ContainerHolder implements MessageAnalyzer,
      RoleHintEnabled {
	private String m_name;

	private int m_hour;

	private int m_index;

	private String[] m_dependencies;

	private int m_errors;

	private ReportManager<R> m_reportManager;

	private MessageFilter m_filter;

	private MessageQueue m_queue;

	private CountDownLatch m_latch = new CountDownLatch(1);

	private AtomicBoolean m_enabled = new AtomicBoolean(true);

	public AbstractMessageAnalyzer(String... dependencies) {
		m_dependencies = dependencies;
		m_queue = new DefaultMessageQueue(getQueueSize());
	}

	@Override
	public void configure(Map<String, String> properties) {
	}

	@Override
	public void doCheckpoint(boolean atEnd) throws IOException {
		shutdown();

		m_reportManager.doCheckpoint(new Date(TimeUnit.HOURS.toMillis(m_hour)), m_index, atEnd);
	}

	@Override
	public void enableRoleHint(String name) {
		m_name = name;
	}

	@Override
	public String[] getDependencies() {
		return m_dependencies;
	}

	protected R getLocalReport(String domain) {
		return m_reportManager.getLocalReport(domain, new Date(TimeUnit.HOURS.toMillis(m_hour)), m_index, true);
	}

	public String getName() {
		Calendar cal = Calendar.getInstance();

		cal.setTimeInMillis(TimeUnit.HOURS.toMillis(m_hour));
		return getClass().getSimpleName() + "-" + cal.get(Calendar.HOUR_OF_DAY) + "-" + m_index;
	}

	@Override
	public MessageQueue getQueue() {
		return m_queue;
	}

	protected int getQueueSize() {
		return 30000;
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

		if (super.hasComponent(MessageFilter.class, m_name)) {
			m_filter = lookup(MessageFilter.class, m_name);
		}

		ReportManagerManager rmm = lookup(ReportManagerManager.class);

		m_reportManager = rmm.getReportManager(m_name);
		m_reportManager.doInitLoad(new Date(TimeUnit.HOURS.toMillis(m_hour)), m_index);
	}

	protected abstract void process(MessageTree tree);

	@Override
	public void run() {
		while (m_enabled.get() || m_queue.size() > 0) {
			MessageTree tree = m_queue.poll();

			if (tree != null) {
				try {
					if (m_filter == null || m_filter.apply(tree)) {
						process(tree);
					}
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
