package org.unidal.cat.plugin.event;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.report.spi.ReportManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageAnalyzer.class, value = EventConstants.ID)
public class EventReportAnalyzer extends AbstractMessageAnalyzer<EventReport> implements LogEnabled {
	@Inject(EventConstants.ID)
	private ReportManager<EventReport> m_reportManager;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		try {
			m_reportManager.doCheckpoint(new Date(getStartTime()), m_index, atEnd);
		} catch (IOException e) {
			Cat.logError(e);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Deprecated
	@Override
	public int getAnanlyzerCount() {
		return 2;
	}

	@Deprecated
	@Override
	public EventReport getReport(String domain) {
		return null;
	}

	@Deprecated
	@Override
	public com.dianping.cat.report.ReportManager<EventReport> getReportManager() {
		return null;
	}

	@Override
	protected void loadReports() {
		try {
			m_reportManager.doInitLoad(new Date(getStartTime()), m_index);
		} catch (IOException e) {
			Cat.logError(e);
		}
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		EventReport report = m_reportManager.getLocalReport(domain, new Date(getStartTime()), m_index, true);
		Message message = tree.getMessage();
		String ip = tree.getIpAddress();

		report.addIp(ip);

		if (message instanceof Transaction) {
			processTransaction(report, tree, (Transaction) message);
		} else if (message instanceof Event) {
			processEvent(report, tree, (Event) message);
		}
	}

	private void processEvent(EventReport report, MessageTree tree, Event event) {
		int count = 1;
		EventType type = report.findOrCreateMachine(tree.getIpAddress()).findOrCreateType(event.getType());
		EventName name = type.findOrCreateName(event.getName());
		String messageId = tree.getMessageId();

		report.addIp(tree.getIpAddress());
		type.incTotalCount(count);
		name.incTotalCount(count);

		if (event.isSuccess()) {
			if (type.getSuccessMessageUrl() == null) {
				type.setSuccessMessageUrl(messageId);
			}

			if (name.getSuccessMessageUrl() == null) {
				name.setSuccessMessageUrl(messageId);
			}
		} else {
			type.incFailCount(count);
			name.incFailCount(count);

			if (type.getFailMessageUrl() == null) {
				type.setFailMessageUrl(messageId);
			}

			if (name.getFailMessageUrl() == null) {
				name.setFailMessageUrl(messageId);
			}
		}
		type.setFailPercent(type.getFailCount() * 100.0 / type.getTotalCount());
		name.setFailPercent(name.getFailCount() * 100.0 / name.getTotalCount());

		processEventGraph(name, event, count);
	}

	private void processEventGraph(EventName name, Event t, int count) {
		long current = t.getTimestamp() / 1000 / 60;
		int min = (int) (current % (60));
		Range range = name.findOrCreateRange(min);

		range.incCount(count);
		if (!t.isSuccess()) {
			range.incFails(count);
		}
	}

	private void processTransaction(EventReport report, MessageTree tree, Transaction t) {
		List<Message> children = t.getChildren();

		for (Message child : children) {
			if (child instanceof Transaction) {
				processTransaction(report, tree, (Transaction) child);
			} else if (child instanceof Event) {
				processEvent(report, tree, (Event) child);
			}
		}
	}
}
