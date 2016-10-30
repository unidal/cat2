package org.unidal.cat.plugin.transaction.alert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.unidal.cat.core.alert.metric.MetricsBuilder;
import org.unidal.cat.core.alert.model.entity.AlertEvent;
import org.unidal.cat.core.alert.model.entity.AlertMetric;
import org.unidal.cat.core.alert.rule.AlertRuleService;
import org.unidal.cat.core.alert.rules.entity.AlertRuleSetDef;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.model.entity.TransactionName;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.model.entity.TransactionType;
import org.unidal.cat.plugin.transaction.model.transform.BaseVisitor;
import org.unidal.cat.spi.report.ReportManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = MetricsBuilder.class, value = TransactionConstants.NAME)
public class TransactionMetricsBuilder implements MetricsBuilder {
   @Inject(type = ReportManager.class, value = TransactionConstants.NAME)
   private ReportManager<TransactionReport> m_manager;

   @Inject
   private AlertRuleService m_service;

   @Override
   public void build(AlertEvent event) {
      String type = TransactionConstants.NAME;
      int hour = (int) TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis());
      List<Map<String, TransactionReport>> list = m_manager.getLocalReports(hour);
      Set<String> domains = m_service.getAttributes(type, "domain");

      for (Map<String, TransactionReport> item : list) {
         for (Map.Entry<String, TransactionReport> e : item.entrySet()) {
            String domain = e.getKey();

            if (domains.contains(domain)) {
               List<AlertRuleSetDef> rules = m_service.getRuleSetByAttribute(type, "domain", domain);
               TransactionReport report = e.getValue();
               Collector visitor = new Collector(event, domain, rules);

               report.accept(visitor);
            }
         }
      }
   }

   static class Collector extends BaseVisitor {
      private AlertEvent m_event;

      private String m_domain;

      private String m_type;

      private Map<String, Set<String>> m_typeNamesMap;

      public Collector(AlertEvent event, String domain, List<AlertRuleSetDef> rules) {
         m_event = event;
         m_domain = domain;
         m_typeNamesMap = new HashMap<String, Set<String>>();

         for (AlertRuleSetDef rule : rules) {
            String type = rule.getDynamicAttribute("type");
            String name = rule.getDynamicAttribute("name");
            Set<String> names = m_typeNamesMap.get(type);

            if (names == null) {
               names = new HashSet<String>();
               m_typeNamesMap.put(type, names);
            }

            names.add(name);
         }
      }

      @Override
      public void visitName(TransactionName name) {
         Set<String> names = m_typeNamesMap.get(m_type);

         if (names != null && names.contains(name.getId())) {
            AlertMetric metric = new AlertMetric();

            metric.set("domain", m_domain);
            metric.set("type", m_type);
            metric.set("name", name.getId());
            metric.set("total", name.getTotalCount());
            metric.set("fail", name.getFailCount());
            metric.set("min", name.getMin());
            metric.set("max", name.getMax());
            metric.set("duration", name.getSum());

            m_event.addMetric(metric);
         }
      }

      @Override
      public void visitType(TransactionType type) {
         m_type = type.getId();
         super.visitType(type);
      }
   }
}
