package org.unidal.cat.spi.analysis;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.spi.Report;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.analysis.MessageAnalyzerManager;
import com.dianping.cat.message.spi.MessageTree;

public class MessageAnalyzerManagerTest extends ComponentTestCase {
   @Test
   public void test() throws Exception {
      defineComponent(MessageAnalyzer.class, "mock", MockAnalyzer.class);

      MessageAnalyzerManager manager = lookup(MessageAnalyzerManager.class);
      List<String> names = manager.getAnalyzerNames();

      Assert.assertEquals(true, names.size() >= 1);
   }

   public static final class MockAnalyzer extends AbstractMessageAnalyzer<Report> {
      public MockAnalyzer() {
         super("mock");
      }

      @Override
      public void configure(Map<String, String> properties) {
      }

      @Override
      public void initialize(int index, int hour) throws IOException {
      }

      @Override
      protected void process(MessageTree tree) {
      }
   }
}
