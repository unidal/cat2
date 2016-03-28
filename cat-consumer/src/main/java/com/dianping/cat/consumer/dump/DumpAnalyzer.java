package com.dianping.cat.consumer.dump;

import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.message.storage.MessageDumper;
import org.unidal.cat.message.storage.MessageDumperManager;
import org.unidal.cat.message.storage.MessageFinderManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.statistic.ServerStatisticManager;

@Named(type = MessageAnalyzer.class, value = DumpAnalyzer.ID, instantiationStrategy = Named.PER_LOOKUP)
public class DumpAnalyzer extends AbstractMessageAnalyzer<Object> implements LogEnabled {
	public static final String ID = "dump";

	@Inject
	private ServerStatisticManager m_serverStateManager;

	@Inject
	private MessageDumperManager m_dumperManager;

	@Inject
	private MessageFinderManager m_finderManager;

	private Logger m_logger;

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		try {
			int hour = (int) TimeUnit.MILLISECONDS.toHours(m_startTime);

			m_dumperManager.close(hour);
			m_finderManager.close(hour);
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public Object getReport(String domain) {
		throw new UnsupportedOperationException("This should not be called!");
	}

	@Override
	public ReportManager<?> getReportManager() {
		return null;
	}

	@Override
	public void initialize(long startTime, long duration, long extraTime) {
		super.initialize(startTime, duration, extraTime);

		int hour = (int) TimeUnit.MILLISECONDS.toHours(startTime);
		m_dumperManager.findOrCreate(hour);
	}

	@Override
	protected void loadReports() {
		// do nothing
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();

		if ("PhoenixAgent".equals(domain)) {
			return;
		} else {
			MessageId messageId = MessageId.parse(tree.getMessageId());
			int hour = messageId.getHour();
			
			tree.setFormatMessageId(messageId);
			
			MessageDumper dumper = m_dumperManager.find(hour);

			if (dumper != null) {
				dumper.process(tree);
			} else {
				m_serverStateManager.addPigeonTimeError(1);
			}
		}
	}

	public void setServerStateManager(ServerStatisticManager serverStateManager) {
		m_serverStateManager = serverStateManager;
	}

}
