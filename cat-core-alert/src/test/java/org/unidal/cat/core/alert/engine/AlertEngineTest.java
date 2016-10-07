package org.unidal.cat.core.alert.engine;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.core.alert.model.entity.AlertEvent;
import org.unidal.cat.core.alert.model.entity.AlertMachine;
import org.unidal.cat.core.alert.model.entity.AlertMetric;
import org.unidal.cat.core.alert.model.entity.AlertReport;
import org.unidal.cat.core.alert.service.AlertReportService;
import org.unidal.helper.Threads;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Named;

public class AlertEngineTest extends ComponentTestCase {
   @Test
   public void test() throws Exception {
      defineComponent(AlertReportService.class, MockAlertReportService.class);

      AlertEngine engine = lookup(AlertEngine.class);

      engine.register(new MockAlertListener());
      Threads.forGroup("cat").start(engine);

      TimeUnit.MILLISECONDS.sleep(100);
      engine.shutdown();
   }

   private static class MockAlertListener implements AlertListener {
      @Override
      public Class<? extends AlertEventSource> getEventClass() {
         return MockEvent.class;
      }

      @Override
      public String getEventName() {
         return "mock";
      }

      @Override
      public String getStatement() {
         return "select typeName,fromIp,total,fail,sum(total) as totalSum, sum(fail) as failSum from mock.win:length_batch(3)";
      }

      @Override
      public void onEvent(AlertContext ctx) {
         Assert.assertEquals(3, ctx.getRows());

         for (int i = 0; i < ctx.getRows(); i++) {
            Assert.assertEquals("mock", ctx.getCell(i, "typeName"));
            Assert.assertEquals("localhost", ctx.getCell(i, "fromIp"));
            Assert.assertEquals(68, ctx.getCell(i, "totalSum"));
            Assert.assertEquals(47, ctx.getCell(i, "failSum"));
         }

         System.out.println("Listener triggered!");
      }
   }

   @Named(type = AlertReportService.class)
   public static class MockAlertReportService implements AlertReportService {
      @Override
      public AlertReport getReport() {
         return new AlertReport().addMachine(new AlertMachine("localhost").addEvent(new AlertEvent("mock") //
               .addMetric(new AlertMetric().set("total", "10").set("fail", "3")) //
               .addMetric(new AlertMetric().set("total", "22").set("fail", "18")) //
               .addMetric(new AlertMetric().set("total", "36").set("fail", "26")) //
               ));
      }
   }

   static class MockEvent extends AbstractEventSource {
      private int m_total;

      private int m_fail;

      public MockEvent(AlertMetric metric) {
         super(metric);

         m_total = getInt("total", 0);
         m_fail = getInt("fail", 0);
      }

      public int getFail() {
         return m_fail;
      }

      public int getTotal() {
         return m_total;
      }
   }
}
