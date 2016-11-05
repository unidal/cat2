package org.unidal.cat.plugin.transaction.alert;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.core.alert.metric.MetricsBuilder;
import org.unidal.cat.core.alert.model.entity.AlertEvent;
import org.unidal.cat.core.alert.rule.RuleService;
import org.unidal.cat.core.alert.rule.entity.AlertRuleSetDef;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.model.entity.Machine;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.model.entity.TransactionType;
import org.unidal.cat.spi.report.ReportManager;
import org.unidal.cat.spi.report.internals.AbstractReportManager;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Named;

public class TransactionMetricsBuilderTest extends ComponentTestCase {
   @Test
   public void test() throws Exception {
      defineComponent(RuleService.class, MockAlertRuleService.class);
      defineComponent(ReportManager.class, TransactionConstants.NAME, MockTransactionReportManager.class);

      MetricsBuilder builder = lookup(MetricsBuilder.class, TransactionConstants.NAME);
      AlertEvent event = new AlertEvent();

      builder.build(event);

      // System.out.println(event);
      Assert.assertEquals(9, event.getMetrics().size());
   }

   @Named(type = RuleService.class)
   public static class MockAlertRuleService implements RuleService {
      @Override
      public Set<String> getAttributes(String type, String name) {
         return setOf("cat1", "cat3", "cat5");
      }

      @Override
      public List<AlertRuleSetDef> getRuleSetByAttribute(String type, String name, String value) {
         List<AlertRuleSetDef> rules = new ArrayList<AlertRuleSetDef>();
         int val = Integer.parseInt(value.substring(3));

         for (int i = 1; i <= val; i++) {
            rules.add(new AlertRuleSetDef().set("type", "type" + i).set("name", "name" + i));
         }

         return rules;
      }

      @Override
      public List<AlertRuleSetDef> getRuleSets() {
         throw new UnsupportedOperationException();
      }

      @Override
      public Set<String> getTypes() {
         return setOf(TransactionConstants.NAME);
      }

      private Set<String> setOf(String... values) {
         Set<String> set = new LinkedHashSet<String>();

         for (String value : values) {
            set.add(value);
         }
         return set;
      }
   }

   @Named(type = ReportManager.class, value = TransactionConstants.NAME)
   public static class MockTransactionReportManager extends AbstractReportManager<TransactionReport> {
      @Override
      public List<Map<String, TransactionReport>> getLocalReports(int hour) {
         List<Map<String, TransactionReport>> list = new ArrayList<Map<String, TransactionReport>>();
         Map<String, TransactionReport> map = new LinkedHashMap<String, TransactionReport>();

         for (int i = 0; i < 6; i++) {
            String domain = "cat" + i;
            TransactionReport report = new TransactionReport();
            Machine m = report.findOrCreateMachine("ip");

            for (int j = 0; j <= i; j++) {
               TransactionType t = m.findOrCreateType("type" + j);

               t.findOrCreateName("name" + j).setMin(0).setMax(0);
            }

            map.put(domain, report);
         }

         list.add(map);

         return list;
      }
   }
}
