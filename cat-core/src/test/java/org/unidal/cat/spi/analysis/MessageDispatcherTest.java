package org.unidal.cat.spi.analysis;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.cat.message.TreeHelper;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportManagerManager;
import org.unidal.cat.spi.analysis.event.TimeWindowHandler;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;

public class MessageDispatcherTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		defineComponent(MessageAnalyzer.class, "mock", MockAnalyzer.class) //
		      .is(PER_LOOKUP).req(ReportManagerManager.class);

		MessageAnalyzerManager manager = lookup(MessageAnalyzerManager.class);
		MessageDispatcher dispatcher = lookup(MessageDispatcher.class);
		TimeWindowHandler handler = (TimeWindowHandler) dispatcher;

		for (int hours = 0; hours < 3; hours++) {
			int hour = 405439 + hours;

			handler.onTimeWindowEnter(hour);

			for (int i = 0; i < 100; i++) {
				MessageTree tree = TreeHelper.tree(new MessageId("mock", "7f000001", hour, i));

				dispatcher.dispatch(tree);
			}

			handler.onTimeWindowExit(hour);
			manager.doCheckpoint(hour, true);
		}

		Assert.assertEquals("{405439=100, 405440=100, 405441=100}", MockAnalyzer.getResult());
	}

	public static final class MockAnalyzer extends AbstractMessageAnalyzer<Report> {
		private static Map<Integer, AtomicInteger> s_counts = new TreeMap<Integer, AtomicInteger>();

		private int m_hour;

		public MockAnalyzer() {
			super("mock");
		}

		@Override
		public void initialize(int index, int hour) throws IOException {
			m_hour = hour;
		}

		public static String getResult() {
			return s_counts.toString();
		}

		@Override
		public void configure(Map<String, String> properties) {
		}

		@Override
		public void doCheckpoint(boolean atEnd) throws IOException {
			shutdown();
		}

		@Override
		protected void process(MessageTree tree) {
			AtomicInteger count = s_counts.get(m_hour);

			if (count == null) {
				count = new AtomicInteger();
				s_counts.put(m_hour, count);
			}

			count.incrementAndGet();
		}
	}
}
