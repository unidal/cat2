package org.unidal.cat.internals;

import java.util.Map;

import org.unidal.initialization.DefaultModuleContext;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;
import org.unidal.initialization.ModuleInitializer;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.CatClientModule;
import com.dianping.cat.message.MessageProducer;

@Named
public class CatClientInitializer extends ContainerHolder {
   public MessageProducer initialize(Map<String, Object> properties) {
      try {
         Module module = lookup(Module.class, CatClientModule.ID);

         if (!module.isInitialized()) {
            ModuleInitializer initializer = lookup(ModuleInitializer.class);
            ModuleContext ctx = new DefaultModuleContext(getContainer());
            long start = System.currentTimeMillis();

            for (Map.Entry<String, Object> e : properties.entrySet()) {
               ctx.setAttribute(e.getKey(), e.getValue());
            }

            String serverMode = System.getProperty("ServerMode");

            if (serverMode != null && "true".equals(serverMode)) {
               initializer.execute(ctx, module);
            } else {
               ctx.info("Start to initialize CAT ...");
               initializer.execute(ctx, module);

               ctx.info("CAT is initialized successfully in " + (System.currentTimeMillis() - start) + " ms.");
            }
         }

         return lookup(MessageProducer.class);
      } catch (Throwable t) {
         System.err.println("[ERROR] Exception occurs while initializing, CAT was DISABLED!");
         t.printStackTrace();
      }

      return null;
   }
}
