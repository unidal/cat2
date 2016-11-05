package org.unidal.cat.plugin.transaction.alert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.unidal.cat.core.alert.metric.Metrics;
import org.unidal.cat.core.alert.metric.MetricsBuilder;
import org.unidal.cat.core.alert.model.entity.AlertEvent;
import org.unidal.cat.core.alert.model.entity.AlertMetric;
import org.unidal.cat.core.alert.rule.RuleService;
import org.unidal.cat.core.alert.rule.entity.AlertRuleSetDef;
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
   private RuleService m_service;

   @Override
   public void build(AlertEvent event) {
      String type = TransactionConstants.NAME;
      int hour = (int) TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis());
      List<Map<String, TransactionReport>> list = m_manager.getLocalReports(hour);

      if (!list.isEmpty()) {
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
   }

   @Override
   public Class<? extends Metrics> getMetricsType() {
      return TransactionMetrics.class;
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

            for (TransactionField field : TransactionField.values()) {
               switch (field) {
               case DOMAIN:
                  metric.set(field.getName(), m_domain);
                  break;
               case TYPE:
                  metric.set(field.getName(), m_type);
                  break;
               case NAME:
                  metric.set(field.getName(), name.getId());
                  break;
               case HITS:
                  metric.set(field.getName(), name.getTotalCount());
                  break;
               case FAILS:
                  metric.set(field.getName(), name.getFailCount());
                  break;
               case DURATION:
                  metric.set(field.getName(), name.getSum());
                  break;
               }
            }

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
