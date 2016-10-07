package org.unidal.cat.core.alert.service;

import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.alert.AlertMetricBuilder;
import org.unidal.cat.core.alert.model.entity.AlertEvent;
import org.unidal.cat.core.alert.model.entity.AlertMachine;
import org.unidal.cat.core.alert.model.entity.AlertReport;
import org.unidal.helper.Inets;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = AlertReportBuilder.class)
public class LocalAlertReportBuilder extends ContainerHolder implements AlertReportBuilder, Initializable {
   private Map<String, AlertMetricBuilder> m_builders;

   @Override
   public AlertReport build() {
      String ip = Inets.IP4.getLocalHostAddress();
      AlertMachine machine = new AlertMachine(ip);

      for (Map.Entry<String, AlertMetricBuilder> e : m_builders.entrySet()) {
         AlertEvent event = new AlertEvent(e.getKey());
         AlertMetricBuilder builder = e.getValue();

         machine.addEvent(event);

         try {
            builder.build(event);
         } catch (Throwable t) {
            Cat.logError(t);
         }
      }

      AlertReport report = new AlertReport();

      report.addMachine(machine);
      return report;
   }

   @Override
   public void initialize() throws InitializationException {
      m_builders = lookupMap(AlertMetricBuilder.class);
   }
}
