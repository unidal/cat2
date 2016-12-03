package org.unidal.cat.core.report;

import org.unidal.cat.CatConstant;
import org.unidal.cat.core.config.CatConfigModule;
import org.unidal.cat.spi.analysis.MessageDispatcher;
import org.unidal.cat.spi.analysis.event.TimeWindowManager;
import org.unidal.cat.spi.report.task.ReportTaskConsumer;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;
import org.unidal.lookup.annotation.Named;

@Named(type = Module.class, value = CatReportModule.ID)
public class CatReportModule extends AbstractModule {
   public static final String ID = "cat-core-report";

   @Override
   public Module[] getDependencies(ModuleContext ctx) {
      return ctx.getModules(CatConfigModule.ID);
   }

   @Override
   protected void execute(ModuleContext ctx) throws Exception {
      ctx.lookup(MessageDispatcher.class);

      TimeWindowManager manager = ctx.lookup(TimeWindowManager.class);

      if (manager instanceof Task) {
         Threads.forGroup(CatConstant.CAT).start((Task) manager);
      }

      ReportTaskConsumer consumer = ctx.lookup(ReportTaskConsumer.class);

      Threads.forGroup(CatConstant.CAT).start(consumer);
   }
}
