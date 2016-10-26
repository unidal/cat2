package org.unidal.cat.plugin.transaction.alert;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.unidal.cat.core.alert.metric.Metrics;
import org.unidal.cat.core.alert.metric.MetricsListener;
import org.unidal.cat.core.alert.model.entity.AlertMetric;
import org.unidal.cat.core.alert.rule.AlertRuleService;
import org.unidal.cat.core.alert.rules.entity.AlertModel;
import org.unidal.cat.core.alert.rules.entity.AlertRuleSet;
import org.unidal.cat.core.alert.rules.transform.DefaultSaxParser;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Named;

public class TransactionMetricsListenerTest extends ComponentTestCase {
   private Metrics metrics(String domain, String type, String name, int hits) {
      AlertMetric m = new AlertMetric();

      m.set("domain", domain).set("type", type).set("name", name).set("hits", String.valueOf(hits));
      return new TransactionMetrics(m);
   }

   @Test
   @SuppressWarnings({ "rawtypes", "unchecked" })
   public void test() throws Exception {
      defineComponent(AlertRuleService.class, MockAlertRuleService.class);

      MetricsListener listener = lookup(MetricsListener.class, TransactionConstants.NAME);

      Threads.forGroup("cat").start((Task) listener);

      listener.onMetrics(metrics("cat1", "type1", "name1", 140));
      listener.onMetrics(metrics("cat1", "type1", "name1", 200));
      listener.onMetrics(metrics("cat1", "type1", "name1", 120));
      
      listener.onMetrics(metrics("cat2", "type1", "name1", 140));
      listener.onMetrics(metrics("cat2", "type1", "name1", 200));
      listener.onMetrics(metrics("cat2", "type1", "name1", 120));

      System.out.println("Press any key to continue ...");
      System.in.read();
   }

   @Named(type = AlertRuleService.class)
   public static class MockAlertRuleService implements AlertRuleService {
      @Override
      public Set<String> getAttributes(String type, String name) {
         throw new UnsupportedOperationException();
      }

      @Override
      public List<AlertRuleSet> getRuleSetByAttribute(String type, String name, String value) {
         throw new UnsupportedOperationException();
      }

      @Override
      public List<AlertRuleSet> getRuleSets() {
         try {
            InputStream in = getClass().getResourceAsStream("transaction-rules.xml");
            AlertModel model = DefaultSaxParser.parse(in);

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
