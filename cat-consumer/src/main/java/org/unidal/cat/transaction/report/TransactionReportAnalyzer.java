package org.unidal.cat.transaction.report;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.report.spi.ReportManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.Range2;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageAnalyzer.class, value = TransactionReportAnalyzer.ID)
public class TransactionReportAnalyzer extends AbstractMessageAnalyzer<TransactionReport> implements LogEnabled {
	public static final String ID = "transaction";

	@Inject(ID)
	private ReportManager<TransactionReport> m_reportManager;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	private Pair<Boolean, Long> checkForTruncatedMessage(MessageTree tree, Transaction t) {
		Pair<Boolean, Long> pair = new Pair<Boolean, Long>(true, t.getDurationInMicros());
		List<Message> children = t.getChildren();
		int size = children.size();

		if (tree.getMessage() == t && size > 0) { // root transaction with children
			Message last = children.get(size - 1);

			if (last instanceof Event) {
				String type = last.getType();
				String name = last.getName();

				if (type.equals("RemoteCall") && name.equals("Next")) {
					pair.setKey(false);
				} else if (type.equals("TruncatedTransaction") && name.equals("TotalDuration")) {
					try {
						long delta = Long.parseLong(last.getData().toString());

						pair.setValue(delta);
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		}

		return pair;
	}

	private double computeDuration(double duration) {
		if (duration < 20) {
			return duration;
		} else if (duration < 200) {
			return duration - duration % 5;
		} else if (duration < 2000) {
			return duration - duration % 50;
		} else {
			return duration - duration % 500;
		}
	}

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

	@Override
	public int getAnanlyzerCount() {
		return 2;
	}

	@Override
	public TransactionReport getReport(String domain) {
		return null;
	}

	@Override
	public com.dianping.cat.report.ReportManager<?> getReportManager() {
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
		TransactionReport report = m_reportManager.getCurrentReport(domain, new Date(getStartTime()), m_index, true);
		Message message = tree.getMessage();

		report.addIp(tree.getIpAddress());

		if (message instanceof Transaction) {
			Transaction root = (Transaction) message;

			processTransaction(report, tree, root);
		}
	}

	private void processNameGraph(Transaction t, TransactionName name, int min, double d) {
		int dk = 1;

		if (d > 65536) {
			dk = 65536;
		} else {
			if (dk > 256) {
				dk = 256;
			}
			while (dk < d) {
				dk <<= 1;
			}
		}

		Duration duration = name.findOrCreateDuration(dk);
		Range range = name.findOrCreateRange(min);

		duration.incCount();
		range.incCount();

		if (!t.isSuccess()) {
			range.incFails();
		}

		range.setSum(range.getSum() + d);
	}

	protected void processTransaction(TransactionReport report, MessageTree tree, Transaction t) {
		String type = t.getType();
		String name = t.getName();

		if (m_serverFilterConfigManager.discardTransaction(type, name)) {
			return;
		} else {
			Pair<Boolean, Long> pair = checkForTruncatedMessage(tree, t);

			if (pair.getKey().booleanValue()) {
				String ip = tree.getIpAddress();
				TransactionType transactionType = report.findOrCreateMachine(ip).findOrCreateType(type);
				TransactionName transactionName = transactionType.findOrCreateName(name);
				String messageId = tree.getMessageId();

				processTypeAndName(t, transactionType, transactionName, messageId, pair.getValue().doubleValue() / 1000d);
			}

			List<Message> children = t.getChildren();

			for (Message child : children) {
				if (child instanceof Transaction) {
					processTransaction(report, tree, (Transaction) child);
				}
			}
		}
	}

	protected void processTypeAndName(Transaction t, TransactionType type, TransactionName name, String messageId,
	      double duration) {
		type.incTotalCount();
		name.incTotalCount();

		if (t.isSuccess()) {
			if (type.getSuccessMessageUrl() == null) {
				type.setSuccessMessageUrl(messageId);
			}

			if (name.getSuccessMessageUrl() == null) {
				name.setSuccessMessageUrl(messageId);
			}
		} else {
			type.incFailCount();
			name.incFailCount();

			if (type.getFailMessageUrl() == null) {
				type.setFailMessageUrl(messageId);
			}

			if (name.getFailMessageUrl() == null) {
				name.setFailMessageUrl(messageId);
			}
		}

		int allDuration = ((int) computeDuration(duration));
		double sum = duration * duration;

		name.setMax(Math.max(name.getMax(), duration));
		name.setMin(Math.min(name.getMin(), duration));
		name.setSum(name.getSum() + duration);
		name.setSum2(name.getSum2() + sum);
		name.findOrCreateAllDuration(allDuration).incCount();

		type.setMax(Math.max(type.getMax(), duration));
		type.setMin(Math.min(type.getMin(), duration));
		type.setSum(type.getSum() + duration);
		type.setSum2(type.getSum2() + sum);
		type.findOrCreateAllDuration(allDuration).incCount();

		long current = t.getTimestamp() / 1000 / 60;
		int min = (int) (current % (60));

		processNameGraph(t, name, min, duration);
		processTypeRange(t, type, min, duration);
	}

	private void processTypeRange(Transaction t, TransactionType type, int min, double d) {
		Range2 range = type.findOrCreateRange2(min);

		if (!t.isSuccess()) {
			range.incFails();
		}

		range.incCount();
		range.setSum(range.getSum() + d);
	}
}
