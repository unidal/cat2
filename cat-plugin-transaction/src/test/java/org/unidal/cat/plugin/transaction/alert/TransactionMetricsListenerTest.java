package org.unidal.cat.plugin.transaction.alert;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.core.alert.message.AlertMessage;
import org.unidal.cat.core.alert.message.AlertMessageSink;
import org.unidal.cat.core.alert.metric.Metrics;
import org.unidal.cat.core.alert.metric.MetricsListener;
import org.unidal.cat.core.alert.model.entity.AlertMetric;
import org.unidal.cat.core.alert.rule.RuleService;
import org.unidal.cat.core.alert.rule.entity.AlertModelDef;
import org.unidal.cat.core.alert.rule.entity.AlertRuleSetDef;
import org.unidal.cat.core.alert.rule.transform.DefaultSaxParser;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Named;

public class TransactionMetricsListenerTest extends ComponentTestCase {
   private Metrics metrics(String name, String ip, int hits, int fails, long duration) {
      AlertMetric m = new AlertMetric();

      m.setTypeName("transaction").setFromIp(ip);
      m.set("domain", "cat").set("type", "URL").set("name", name);
      m.set("hits", String.valueOf(hits));
      m.set("fails", String.valueOf(fails));
      m.set("duration", String.valueOf(duration));

      return new TransactionMetrics(m);
   }

   @Test
   @SuppressWarnings({ "rawtypes", "unchecked" })
   public void test() throws Exception {
      defineComponent(RuleService.class, MockAlertRuleService.class);
      defineComponent(AlertMessageSink.class, MockAlertMessageSink.class);

      MetricsListener listener = lookup(MetricsListener.class, TransactionConstants.NAME);

      Threads.forGroup("cat").start((Task) listener);

      listener.onMetrics(metrics("/cat/r/t", "ip1", 140, 1, 300));
      listener.onMetrics(metrics("/cat/r/t", "ip2", 200, 8, 300));
      listener.onMetrics(metrics("/cat/r/t", "ip3", 120, 3, 1300));

      listener.onMetrics(metrics("/cat/r/m", "ip1", 140, 2, 300));
      listener.onMetrics(metrics("/cat/r/m", "ip2", 200, 4, 300));
      listener.onMetrics(metrics("/cat/r/m", "ip3", 120, 3, 1300));

      listener.checkpoint();

      listener.onMetrics(metrics("/cat/r/t", "ip1", 140, 1, 300));
      listener.onMetrics(metrics("/cat/r/t", "ip2", 200, 8, 300));
      listener.onMetrics(metrics("/cat/r/t", "ip3", 120, 3, 1300));

      listener.onMetrics(metrics("/cat/r/m", "ip1", 140, 12, 300));
      listener.onMetrics(metrics("/cat/r/m", "ip2", 200, 14, 300));
      listener.onMetrics(metrics("/cat/r/m", "ip3", 120, 23, 1300));

      listener.checkpoint();

      listener.onMetrics(metrics("/cat/r/t", "ip1", 140, 1, 300));
      listener.onMetrics(metrics("/cat/r/t", "ip2", 200, 8, 300));
      listener.onMetrics(metrics("/cat/r/t", "ip3", 120, 3, 1300));

      listener.onMetrics(metrics("/cat/r/m", "ip1", 140, 32, 300));
      listener.onMetrics(metrics("/cat/r/m", "ip2", 200, 24, 300));
      listener.onMetrics(metrics("/cat/r/m", "ip3", 120, 36, 1300));

      listener.checkpoint();

      if (listener instanceof Task) {
         ((Task) listener).shutdown();
      }

      AlertMessageSink sink = lookup(AlertMessageSink.class);
      Assert.assertEquals("", sink.toString());
   }

   @Named(type = AlertMessageSink.class)
   public static class MockAlertMessageSink implements AlertMessageSink {
      private StringBuilder m_sb = new StringBuilder(1024);

      @Override
      public void add(AlertMessage m) {
         m_sb.append(m).append("\r\n");
      }

      @Override
      public String toString() {
         return m_sb.toString();
      }
   }

   @Named(type = RuleService.class)
   public static class MockAlertRuleService implements RuleService {
      @Override
      public Set<String> getAttributes(String type, String name) {
         throw new UnsupportedOperationException();
      }

      @Override
      public List<AlertRuleSetDef> getRuleSetByAttribute(String type, String name, String value) {
         throw new UnsupportedOperationException();
      }

      @Override
      public List<AlertRuleSetDef> getRuleSets() {
         try {
            InputStream in = getClass().getResourceAsStream("transaction-rules.xml");
            AlertModelDef model = DefaultSaxParser.parse(in);

            return model.getRuleSets();
         } catch (Exception e) {
            throw new IllegalStateException("Error when loading transaction-rules.xml!", e);
         }
      }

      @Override
      public Set<String> getTypes() {
         throw new UnsupportedOperationException();
      }
   }
}
