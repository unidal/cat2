package org.unidal.cat.core.alert.model;

import java.util.Map;
import java.util.Set;

import org.unidal.cat.core.alert.AlertConstants;
import org.unidal.cat.core.alert.metric.MetricsBuilder;
import org.unidal.cat.core.alert.metric.MetricsBuilderManager;
import org.unidal.cat.core.alert.model.entity.AlertEvent;
import org.unidal.cat.core.alert.model.entity.AlertMachine;
import org.unidal.cat.core.alert.model.entity.AlertReport;
import org.unidal.cat.core.alert.rule.RuleService;
import org.unidal.helper.Inets;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

@Named(type = AlertReportBuilder.class)
public class LocalAlertReportBuilder implements AlertReportBuilder {
   @Inject
   private MetricsBuilderManager m_manager;

   @Inject
   private RuleService m_service;

   @Override
   public AlertReport build() {
      String ip = Inets.IP4.getLocalHostAddress();
      AlertMachine machine = new AlertMachine(ip);
      Set<String> types = m_service.getTypes();
      Transaction trx = Cat.newTransaction(AlertConstants.TYPE_ALERT, "LocalReport");

      try {
         for (Map.Entry<String, MetricsBuilder> e : m_manager.getBuilders().entrySet()) {
            String type = e.getKey();

            if (types.contains(type)) {
               MetricsBuilder builder = e.getValue();
               AlertEvent event = new AlertEvent(type);
               Transaction t = Cat.newTransaction(AlertConstants.TYPE_ALERT, builder.getClass().getSimpleName());

               event.setTypeClass(builder.getMetricsType().getName());
               machine.addEvent(event);

               try {
                  builder.build(event);
                  t.setStatus(Message.SUCCESS);
               } catch (Throwable ex) {
                  t.setStatus(ex);
                  Cat.logError(ex);
               } finally {
                  t.complete();
               }
            }
         }

         trx.setStatus(Message.SUCCESS);
      } finally {
         trx.complete();
      }

      AlertReport report = new AlertReport();

      report.addMachine(machine);
      return report;
   }
}
