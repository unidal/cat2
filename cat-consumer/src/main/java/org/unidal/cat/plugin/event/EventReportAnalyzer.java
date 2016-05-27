package org.unidal.cat.plugin.event;

import java.util.List;

import org.unidal.cat.spi.analysis.AbstractMessageAnalyzer;
import org.unidal.cat.spi.analysis.MessageAnalyzer;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageAnalyzer.class, value = EventConstants.NAME, instantiationStrategy = Named.PER_LOOKUP)
public class EventReportAnalyzer extends AbstractMessageAnalyzer<EventReport> {
	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		EventReport report = getLocalReport(domain);
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
		Range range = findOrCreateRange(name.getRanges(), min);

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

    private Range findOrCreateRange(List<Range> ranges, int min) {
        if (min > ranges.size() - 1) {
            synchronized (ranges) {
                if (min > ranges.size() - 1) {
                    for (int i = ranges.size(); i < 60; i++) {
                        ranges.add(new Range(i));
                    }
                }
            }
        }
        Range range = ranges.get(min);
        return range;
    }
}
