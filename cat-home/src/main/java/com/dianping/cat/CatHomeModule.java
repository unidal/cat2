package com.dianping.cat;

import java.util.concurrent.CountDownLatch;

import org.unidal.cat.core.alert.CatAlertModule;
import org.unidal.cat.core.config.spi.ConfigStore;
import org.unidal.cat.core.config.spi.ConfigStoreManager;
import org.unidal.cat.core.document.CatDocumentModule;
import org.unidal.cat.core.report.CatReportModule;
import org.unidal.helper.Inets;
import org.unidal.helper.Threads;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.DefaultModuleContext;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

import com.dianping.cat.analysis.MessageConsumer;
import com.dianping.cat.analysis.TcpSocketReceiver;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.server.entity.ServerConfig;
import com.dianping.cat.configuration.server.transform.DefaultSaxParser;
import com.dianping.cat.consumer.CatConsumerModule;
import com.dianping.cat.report.alert.app.AppAlert;
import com.dianping.cat.report.alert.business.BusinessAlert;
import com.dianping.cat.report.alert.database.DatabaseAlert;
import com.dianping.cat.report.alert.event.EventAlert;
import com.dianping.cat.report.alert.exception.ExceptionAlert;
import com.dianping.cat.report.alert.exception.FrontEndExceptionAlert;
import com.dianping.cat.report.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.alert.network.NetworkAlert;
import com.dianping.cat.report.alert.storage.StorageCacheAlert;
import com.dianping.cat.report.alert.storage.StorageSQLAlert;
import com.dianping.cat.report.alert.system.SystemAlert;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyAlert;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyAlertBuilder;
import com.dianping.cat.report.alert.transaction.TransactionAlert;
import com.dianping.cat.report.alert.web.WebAlert;
import com.dianping.cat.report.page.ConfigReloadTask;
import com.dianping.cat.report.task.DefaultTaskConsumer;

public class CatHomeModule extends AbstractModule {
   public static final String ID = "cat-home";

   private CountDownLatch m_catServerLatch = new CountDownLatch(1);

   @Override
   protected void execute(ModuleContext ctx) throws Exception {
      ServerConfigManager serverConfigManager = ctx.lookup(ServerConfigManager.class);

      ctx.lookup(MessageConsumer.class);

      ConfigReloadTask configReloadTask = ctx.lookup(ConfigReloadTask.class);
      Threads.forGroup("cat").start(configReloadTask);

      if (serverConfigManager.isJobMachine()) {
         DefaultTaskConsumer taskConsumer = ctx.lookup(DefaultTaskConsumer.class);

         Threads.forGroup("cat").start(taskConsumer);
      }

      if (serverConfigManager.isAlertMachine()) {
         BusinessAlert metricAlert = ctx.lookup(BusinessAlert.class);
         NetworkAlert networkAlert = ctx.lookup(NetworkAlert.class);
         DatabaseAlert databaseAlert = ctx.lookup(DatabaseAlert.class);
         SystemAlert systemAlert = ctx.lookup(SystemAlert.class);
         ExceptionAlert exceptionAlert = ctx.lookup(ExceptionAlert.class);
         FrontEndExceptionAlert frontEndExceptionAlert = ctx.lookup(FrontEndExceptionAlert.class);
         HeartbeatAlert heartbeatAlert = ctx.lookup(HeartbeatAlert.class);
         ThirdPartyAlert thirdPartyAlert = ctx.lookup(ThirdPartyAlert.class);
         ThirdPartyAlertBuilder alertBuildingTask = ctx.lookup(ThirdPartyAlertBuilder.class);
         AppAlert appAlert = ctx.lookup(AppAlert.class);
         WebAlert webAlert = ctx.lookup(WebAlert.class);
         TransactionAlert transactionAlert = ctx.lookup(TransactionAlert.class);
         EventAlert eventAlert = ctx.lookup(EventAlert.class);
         StorageSQLAlert storageDatabaseAlert = ctx.lookup(StorageSQLAlert.class);
         StorageCacheAlert storageCacheAlert = ctx.lookup(StorageCacheAlert.class);

         Threads.forGroup("cat").start(networkAlert);
         Threads.forGroup("cat").start(databaseAlert);
         Threads.forGroup("cat").start(systemAlert);
         Threads.forGroup("cat").start(metricAlert);
         Threads.forGroup("cat").start(exceptionAlert);
         Threads.forGroup("cat").start(frontEndExceptionAlert);
         Threads.forGroup("cat").start(heartbeatAlert);
         Threads.forGroup("cat").start(thirdPartyAlert);
         Threads.forGroup("cat").start(alertBuildingTask);
         Threads.forGroup("cat").start(appAlert);
         Threads.forGroup("cat").start(webAlert);
         Threads.forGroup("cat").start(transactionAlert);
         Threads.forGroup("cat").start(eventAlert);
         Threads.forGroup("cat").start(storageDatabaseAlert);
         Threads.forGroup("cat").start(storageCacheAlert);
      }

      final MessageConsumer consumer = ctx.lookup(MessageConsumer.class);
      final TcpSocketReceiver receiver = ctx.lookup(TcpSocketReceiver.class);

      receiver.setup();

      Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run() {
            consumer.doCheckpoint();
            receiver.destory();
         }
      });
   }

   @Override
   public Module[] getDependencies(ModuleContext ctx) {
      return ctx.getModules(CatConsumerModule.ID, CatReportModule.ID, CatDocumentModule.ID, CatAlertModule.ID);
   }

   @Override
   protected void setup(ModuleContext ctx) throws Exception {
      // let CAT client connect to local CAT server directly
      System.setProperty("cat.servers", "127.0.0.1");
      ((DefaultModuleContext) ctx).getContainer().addContextValue("cat.server.latch", m_catServerLatch);

      ServerConfigManager configManager = ctx.lookup(ServerConfigManager.class);
      ConfigStoreManager manager = ctx.lookup(ConfigStoreManager.class);
      String ip = Inets.IP4.getLocalHostAddress();
      ConfigStore store = manager.getConfigStore("system", "server:" + ip);
      String xml = store.getConfig();

      if (xml != null) {
         ServerConfig config = DefaultSaxParser.parse(xml);

         configManager.initialize(config);
      } else {
         configManager.initialize(new ServerConfig());
      }
   }
}
