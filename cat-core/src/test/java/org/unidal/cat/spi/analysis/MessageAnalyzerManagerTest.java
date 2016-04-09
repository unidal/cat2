package org.unidal.cat.spi.analysis;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportManagerManager;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.spi.MessageTree;

public class MessageAnalyzerManagerTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		defineComponent(MessageAnalyzer.class, "mock", MockAnalyzer.class)//
		      .req(ReportManagerManager.class);

		MessageAnalyzerManager manager = lookup(MessageAnalyzerManager.class);
		List<MessageAnalyzer> analyzers = manager.getAnalyzers(12345);

		Assert.assertEquals(true, analyzers.size() >= 1);
	}

	public static final class MockAnalyzer extends AbstractMessageAnalyzer<Report> {
		public MockAnalyzer() {
			super("mock");
		}

		@Override
		public void initialize(int index, int hour) throws IOException {
		}

		@Override
		public void configure(Map<String, String> properties) {
		}

		@Override
		protected void process(MessageTree tree) {
		}
	}
}
