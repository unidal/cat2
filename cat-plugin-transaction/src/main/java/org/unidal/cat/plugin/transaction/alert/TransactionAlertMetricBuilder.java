package org.unidal.cat.plugin.transaction.alert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.unidal.cat.core.alert.AlertMetricBuilder;
import org.unidal.cat.core.alert.model.entity.AlertEvent;
import org.unidal.cat.core.alert.model.entity.AlertMetric;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.cat.plugin.transaction.model.entity.TransactionName;
import org.unidal.cat.plugin.transaction.model.entity.TransactionReport;
import org.unidal.cat.plugin.transaction.model.entity.TransactionType;
import org.unidal.cat.plugin.transaction.model.transform.BaseVisitor;
import org.unidal.cat.spi.report.ReportManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = AlertMetricBuilder.class, value = TransactionConstants.NAME)
public class TransactionAlertMetricBuilder implements AlertMetricBuilder {
   @Inject(type = ReportManager.class, value = TransactionConstants.NAME)
   private ReportManager<TransactionReport> m_manager;

   @Override
   public void build(AlertEvent event) {
      int hour = (int) TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis());
      List<Map<String, TransactionReport>> list = m_manager.getLocalReports(hour);

      for (Map<String, TransactionReport> item : list) {
         for (Map.Entry<String, TransactionReport> e : item.entrySet()) {
            String domain = e.getKey();
            TransactionReport report = e.getValue();
            Visitor visitor = new Visitor(event, domain);

            report.accept(visitor);
         }
      }
   }

   public class Visitor extends BaseVisitor {
      private AlertEvent m_event;

      private String m_domain;

      private String m_type;

      public Visitor(AlertEvent event, String domain) {
         m_event = event;
         m_domain = domain;
      }

      @Override
      public void visitName(TransactionName name) {
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

      @Override
      public void visitType(TransactionType type) {
         if (type.getId().equals("URL")) {
            m_type = type.getId();
            super.visitType(type);
         }
      }
   }
}
