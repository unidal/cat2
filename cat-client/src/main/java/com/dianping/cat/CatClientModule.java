package com.dianping.cat;

import java.io.File;
import java.util.concurrent.ExecutorService;

import org.unidal.cat.CatConstant;
import org.unidal.cat.config.ClientConfigurationManager;
import org.unidal.cat.config.ClientEnvironmentSettings;
import org.unidal.cat.message.MessageIdFactory;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.AbstractThreadListener;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.DefaultModuleContext;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.analyzer.DataUploader;
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

      // init message id factory
      File baseDir = new File(System.getProperty("java.io.tmpdir"));
      ClientEnvironmentSettings settings = ctx.lookup(ClientEnvironmentSettings.class);

      ctx.lookup(MessageIdFactory.class).initialize(baseDir, settings.getDomain());

      ClientConfigurationManager configManager = ctx.lookup(ClientConfigurationManager.class);

      // bring up TransportManager
      ctx.lookup(TransportManager.class);

      if (configManager.getConfig().isEnabled() && !settings.isTestMode()) {
         // start status update task & data uploader task
         Threads.forGroup(CatConstant.CAT).start(ctx.lookup(StatusUpdateTask.class));
         Threads.forGroup(CatConstant.CAT).start(ctx.lookup(DataUploader.class));
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

         if (ctx instanceof DefaultModuleContext) {
            ((DefaultModuleContext) m_ctx).skipClassForLogger(getClass());
         }
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
