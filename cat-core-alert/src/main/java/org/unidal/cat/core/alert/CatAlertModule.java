package org.unidal.cat.core.alert;

import org.unidal.cat.CatConstant;
import org.unidal.cat.core.alert.config.AlertConfiguration;
import org.unidal.cat.core.alert.metric.MetricsEngine;
import org.unidal.cat.core.config.CatConfigModule;
import org.unidal.helper.Threads;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;
import org.unidal.lookup.annotation.Named;

@Named(type = Module.class, value = CatAlertModule.ID)
public class CatAlertModule extends AbstractModule {
   public static final String ID = "cat-core-alert";

   @Override
   protected void execute(ModuleContext ctx) throws Exception {
      AlertConfiguration configure = ctx.lookup(AlertConfiguration.class);

      if (configure.isEnabled()) {
         MetricsEngine engine = ctx.lookup(MetricsEngine.class);

         Threads.forGroup(CatConstant.CAT).start(engine);
      }
   }

   @Override
   public Module[] getDependencies(ModuleContext ctx) {
      return ctx.getModules(CatConfigModule.ID);
   }
}
