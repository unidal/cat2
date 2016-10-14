package org.unidal.cat.core.alert.service;

import java.util.Map;

import org.unidal.cat.core.alert.metric.MetricsBuilder;
import org.unidal.cat.core.alert.metric.MetricsManager;
import org.unidal.cat.core.alert.model.entity.AlertEvent;
import org.unidal.cat.core.alert.model.entity.AlertMachine;
import org.unidal.cat.core.alert.model.entity.AlertReport;
import org.unidal.helper.Inets;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = AlertReportBuilder.class)
public class LocalAlertReportBuilder implements AlertReportBuilder {
   @Inject
   private MetricsManager m_manager;

   @Override
   public AlertReport build() {
      String ip = Inets.IP4.getLocalHostAddress();
      AlertMachine machine = new AlertMachine(ip);

      for (Map.Entry<String, MetricsBuilder> e : m_manager.getBuilders().entrySet()) {
         AlertEvent event = new AlertEvent(e.getKey());

         machine.addEvent(event);

         try {
            e.getValue().build(event);
         } catch (Throwable t) {
            Cat.logError(t);
         }
      }

      AlertReport report = new AlertReport();

      report.addMachine(machine);
      return report;
   }
}
