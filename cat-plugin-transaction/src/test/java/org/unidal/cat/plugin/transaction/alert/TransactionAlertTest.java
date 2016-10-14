package org.unidal.cat.plugin.transaction.alert;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.unidal.cat.core.alert.metric.MetricsEngine;
import org.unidal.cat.core.alert.model.entity.AlertEvent;
import org.unidal.cat.core.alert.model.entity.AlertMachine;
import org.unidal.cat.core.alert.model.entity.AlertMetric;
import org.unidal.cat.core.alert.model.entity.AlertReport;
import org.unidal.cat.core.alert.service.AlertReportService;
import org.unidal.cat.plugin.transaction.TransactionConstants;
import org.unidal.helper.Threads;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Named;

public class TransactionAlertTest extends ComponentTestCase {
   @Test
   public void test() throws Exception {
      defineComponent(AlertReportService.class, MockAlertReportService.class);

      MetricsEngine engine = lookup(MetricsEngine.class);

      Threads.forGroup("cat").start(engine);

      TimeUnit.MILLISECONDS.sleep(10000);
      engine.shutdown();
   }

   @Named(type = AlertReportService.class)
   public static class MockAlertReportService implements AlertReportService {
      @Override
      public AlertReport getReport() {
         return new AlertReport().addMachine(new AlertMachine("localhost") //
               .addEvent(new AlertEvent(TransactionConstants.NAME) //
                     .setTypeClass(TransactionMetrics.class.getName()) //
                     .addMetric(metric(10, 3, 100)) //
                     .addMetric(metric(22, 18, 330)) //
                     .addMetric(metric(36, 26, 540)) //
                     .addMetric(metric(3, 0, 30)) //
               ));
      }

      private AlertMetric metric(int total, int fail, int duration) {
         return new AlertMetric().set("domain", "cat").set("type", "URL").set("name", "/cat/r/t") //
               .set("total", total).set("fail", fail).set("duration", duration);
      }
   }
}
