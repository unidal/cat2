package org.unidal.cat.core.report.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.report.CatReportModule;
import org.unidal.cat.core.report.config.ReportConfigStoreGroup;
import org.unidal.cat.core.report.nav.DomainGroupBar;
import org.unidal.cat.core.report.remote.DefaultRemoteReportSkeleton;
import org.unidal.cat.core.report.remote.DefaultRemoteReportStub;
import org.unidal.cat.core.report.view.ReportMenuManager;
import org.unidal.cat.core.view.svg.DefaultGraphBuilder;
import org.unidal.cat.core.view.svg.DefaultValueTranslater;
import org.unidal.cat.spi.analysis.DefaultCheckpointService;
import org.unidal.cat.spi.analysis.DefaultMessageDispatcher;
import org.unidal.cat.spi.analysis.DefaultPipelineManager;
import org.unidal.cat.spi.analysis.event.DefaultTimeWindowManager;
import org.unidal.cat.spi.analysis.pipeline.DomainHashStrategy;
import org.unidal.cat.spi.analysis.pipeline.RoundRobinStrategy;
import org.unidal.cat.spi.report.DefaultReportConfiguration;
import org.unidal.cat.spi.report.internals.DefaultReportDelegateManager;
import org.unidal.cat.spi.report.internals.DefaultReportFilterManager;
import org.unidal.cat.spi.report.internals.DefaultReportManagerManager;
import org.unidal.cat.spi.report.internals.DefaultReportReducerManager;
import org.unidal.cat.spi.report.provider.DefaultReportProvider;
import org.unidal.cat.spi.report.provider.HistoricalReportProvider;
import org.unidal.cat.spi.report.provider.RecentReportProvider;
import org.unidal.cat.spi.report.storage.DefaultReportStorage;
import org.unidal.cat.spi.report.storage.FileHistoryReportStorage;
import org.unidal.cat.spi.report.storage.FileHourlyReportStorage;
import org.unidal.cat.spi.report.storage.FileReportStorage;
import org.unidal.cat.spi.report.storage.MysqlHistoryReportStorage;
import org.unidal.cat.spi.report.storage.MysqlHourlyReportStorage;
import org.unidal.cat.spi.report.storage.MysqlReportStorage;
import org.unidal.cat.spi.report.task.internals.DefaultReportTaskConsumer;
import org.unidal.cat.spi.report.task.internals.DefaultReportTaskExecutor;
import org.unidal.cat.spi.report.task.internals.DefaultReportTaskService;
import org.unidal.cat.spi.report.task.internals.DefaultReportTaskTracker;
import org.unidal.cat.spi.report.task.internals.DefaultReportTaskTrackerManager;
import org.unidal.cat.spi.transport.DefaultServerTransportConfiguration;
import org.unidal.cat.spi.transport.DefaultServerTransportHub;
import org.unidal.cat.spi.transport.TcpSocketSkeleton;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
   public static void main(String[] args) {
      generatePlexusComponentsXmlFile(new ComponentsConfigurator());
   }

   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(A(CatReportModule.class));

      all.add(A(ReportMenuManager.class));

      all.add(A(DefaultReportConfiguration.class));
      all.add(A(DefaultReportManagerManager.class));
      all.add(A(DefaultReportFilterManager.class));
      all.add(A(DefaultReportReducerManager.class));
      all.add(A(DefaultReportDelegateManager.class));

      all.add(A(ReportConfigStoreGroup.class));

      all.add(A(DefaultReportTaskConsumer.class));
      all.add(A(DefaultReportTaskExecutor.class));
      all.add(A(DefaultReportTaskService.class));
      all.add(A(DefaultReportTaskTrackerManager.class));
      all.add(A(DefaultReportTaskTracker.class));

      all.add(A(DefaultReportStorage.class));
      all.add(A(FileReportStorage.class));
      all.add(A(FileHourlyReportStorage.class));
      all.add(A(FileHistoryReportStorage.class));
      all.add(A(MysqlReportStorage.class));
      all.add(A(MysqlHourlyReportStorage.class));
      all.add(A(MysqlHistoryReportStorage.class));

      all.add(A(DefaultReportProvider.class));
      all.add(A(RecentReportProvider.class));
      all.add(A(HistoricalReportProvider.class));

      all.add(A(DefaultRemoteReportStub.class));
      all.add(A(DefaultRemoteReportSkeleton.class));

      all.add(A(TcpSocketSkeleton.class));
      all.add(A(DefaultMessageDispatcher.class));
      all.add(A(DefaultServerTransportConfiguration.class));
      all.add(A(DefaultServerTransportHub.class));

      all.add(A(DefaultTimeWindowManager.class));

      all.add(A(DefaultPipelineManager.class));
      all.add(A(DefaultCheckpointService.class));
      all.add(A(DomainHashStrategy.class));
      all.add(A(RoundRobinStrategy.class));

      all.add(A(DomainGroupBar.class));

      all.add(A(DefaultGraphBuilder.class));
      all.add(A(DefaultValueTranslater.class));

      all.addAll(new CatDatabaseConfigurator().defineComponents());
      all.addAll(new WebComponentConfigurator().defineComponents());

      return all;
   }
}
