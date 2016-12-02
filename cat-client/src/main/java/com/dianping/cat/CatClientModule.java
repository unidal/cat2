package com.dianping.cat;

import java.util.concurrent.ExecutorService;

import org.unidal.cat.config.ClientConfigurationManager;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.AbstractThreadListener;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.internal.MilliSecondTimer;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.status.StatusUpdateTask;

@Named(type = Module.class, value = CatClientModule.ID)
public class CatClientModule extends AbstractModule {
   public static final String ID = "cat-client";

   @Override
   protected void execute(final ModuleContext ctx) throws Exception {
      ctx.info("Current working directory is " + System.getProperty("user.dir"));

      // initialize milli-second resolution level timer
      MilliSecondTimer.initialize();

      // tracking thread start/stop
      Threads.addListener(new CatThreadListener(ctx));

      // bring up TransportManager
      ctx.lookup(TransportManager.class);

      ClientConfigurationManager configManager = ctx.lookup(ClientConfigurationManager.class);

      if (configManager.getConfig().isEnabled()) {
         // start status update task
         StatusUpdateTask task = ctx.lookup(StatusUpdateTask.class);

         Threads.forGroup("Cat").start(task);
      }
   }

   @Override
   public Module[] getDependencies(ModuleContext ctx) {
      return null; // no dependencies
   }

   public static final class CatThreadListener extends AbstractThreadListener {
      private final ModuleContext m_ctx;

      private CatThreadListener(ModuleContext ctx) {
         m_ctx = ctx;
      }

      @Override
      public void onThreadGroupCreated(ThreadGroup group, String name) {
         m_ctx.info(String.format("Thread group(%s) created.", name));
      }

      @Override
      public void onThreadPoolCreated(ExecutorService pool, String name) {
         m_ctx.info(String.format("Thread pool(%s) created.", name));
      }

      @Override
      public void onThreadStarting(Thread thread, String name) {
         m_ctx.info(String.format("Starting thread(%s) ...", name));
      }

      @Override
      public void onThreadStopping(Thread thread, String name) {
         m_ctx.info(String.format("Stopping thread(%s).", name));
      }

      @Override
      public boolean onUncaughtException(Thread thread, Throwable e) {
         m_ctx.error(String.format("Uncaught exception thrown out of thread(%s)", thread.getName()), e);
         return true;
      }
   }
}
