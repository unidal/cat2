package com.dianping.cat.consumer.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unidal.cat.plugin.event.EventReportAggregator;
import org.unidal.cat.plugin.event.EventReportAnalyzer;
import org.unidal.cat.plugin.event.EventReportDelegate;
import org.unidal.cat.plugin.event.EventReportManager;
import org.unidal.cat.plugin.event.filter.EventReportFilter;
import org.unidal.cat.plugin.transaction.TransactionReportAggregator;
import org.unidal.cat.plugin.transaction.TransactionReportAnalyzer;
import org.unidal.cat.plugin.transaction.TransactionReportDelegate;
import org.unidal.cat.plugin.transaction.TransactionReportManager;
import org.unidal.cat.plugin.transaction.filter.TransactionNameFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionNameGraphFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionReportHelper;
import org.unidal.cat.plugin.transaction.filter.TransactionTypeFilter;
import org.unidal.cat.plugin.transaction.filter.TransactionTypeGraphFilter;
import org.unidal.initialization.Module;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.config.content.DefaultContentFetcher;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.CatConsumerModule;
import com.dianping.cat.consumer.config.AllReportConfigManager;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.CrossDelegate;
import com.dianping.cat.consumer.cross.IpConvertManager;
import com.dianping.cat.consumer.dal.BusinessReportDao;
import com.dianping.cat.consumer.dependency.DatabaseParser;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyDelegate;
import com.dianping.cat.consumer.dump.DumpAnalyzer;
import com.dianping.cat.consumer.dump.LocalMessageBucketManager;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.EventDelegate;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatDelegate;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixDelegate;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.problem.DefaultProblemHandler;
import com.dianping.cat.consumer.problem.LongExecutionProblemHandler;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.ProblemDelegate;
import com.dianping.cat.consumer.problem.ProblemHandler;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.StateDelegate;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.StorageDelegate;
import com.dianping.cat.consumer.storage.StorageReportUpdater;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.TopDelegate;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionDelegate;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.hadoop.hdfs.HdfsUploader;
import com.dianping.cat.message.PathBuilder;
import com.dianping.cat.message.storage.MessageBucketManager;
import com.dianping.cat.report.DefaultReportManager;
import com.dianping.cat.report.DomainValidator;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.statistic.ServerStatisticManager;
import com.dianping.cat.task.TaskManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(defineReportComponents());

		// all.addAll(defineTransactionComponents());
		// all.addAll(defineEventComponents());
		all.addAll(defineProblemComponents());
		all.addAll(defineHeartbeatComponents());
		all.addAll(defineTopComponents());
		all.addAll(defineDumpComponents());
		all.addAll(defineStateComponents());
		all.addAll(defineCrossComponents());
		all.addAll(defineMatrixComponents());
		all.addAll(defineDependencyComponents());
		all.addAll(defineMetricComponents());
		all.addAll(defineStorageComponents());

		all.add(C(AllReportConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(Module.class, CatConsumerModule.ID, CatConsumerModule.class));
		all.addAll(new CatDatabaseConfigurator().defineComponents());
		return all;
	}

	private Collection<Component> defineCrossComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = CrossAnalyzer.ID;

		all.add(C(IpConvertManager.class));
		all.add(C(MessageAnalyzer.class, ID, CrossAnalyzer.class).is(PER_LOOKUP)
		//
		      .req(ReportManager.class, ID).req(ServerConfigManager.class, IpConvertManager.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP)
		//
		      .req(ReportDelegate.class, ID)
		      //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, CrossDelegate.class).req(TaskManager.class, ServerFilterConfigManager.class));

		return all;
	}

	private Collection<Component> defineDependencyComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = DependencyAnalyzer.ID;

		all.add(C(DatabaseParser.class));
		all.add(C(MessageAnalyzer.class, ID, DependencyAnalyzer.class).is(PER_LOOKUP).req(ReportManager.class, ID)
		      .req(ServerConfigManager.class, ServerFilterConfigManager.class, DatabaseParser.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP)
		//
		      .req(ReportDelegate.class, ID)
		      //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, DependencyDelegate.class).req(TaskManager.class));

		return all;
	}

	private Collection<Component> defineDumpComponents() {
		final List<Component> all = new ArrayList<Component>();

		all.add(A(DumpAnalyzer.class));

		all.add(C(MessageBucketManager.class, LocalMessageBucketManager.ID, LocalMessageBucketManager.class) //
		      .req(ServerConfigManager.class, PathBuilder.class, ServerStatisticManager.class)//
		      .req(HdfsUploader.class));

		return all;
	}

	Collection<Component> defineEventComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = EventAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, EventAnalyzer.class).is(PER_LOOKUP).req(ReportManager.class, ID)
		      .req(ReportDelegate.class, ID).req(ServerConfigManager.class, ServerFilterConfigManager.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP)
		//
		      .req(ReportDelegate.class, ID)
		      //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, EventDelegate.class).req(TaskManager.class, ServerFilterConfigManager.class,
		      AllReportConfigManager.class));

		return all;
	}

	private Collection<Component> defineHeartbeatComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = HeartbeatAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, HeartbeatAnalyzer.class).is(PER_LOOKUP)
		//
		      .req(ReportManager.class, ID).req(ServerConfigManager.class, ServerFilterConfigManager.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP)
		//
		      .req(ReportDelegate.class, ID)
		      //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, HeartbeatDelegate.class).req(TaskManager.class,
		      ServerFilterConfigManager.class));

		return all;
	}

	private Collection<Component> defineMatrixComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = MatrixAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, MatrixAnalyzer.class).is(PER_LOOKUP) //
		      .req(ReportManager.class, ID).req(ServerConfigManager.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP)
		//
		      .req(ReportDelegate.class, ID)
		      //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, MatrixDelegate.class).req(TaskManager.class, ServerFilterConfigManager.class));

		return all;
	}

	private Collection<Component> defineMetricComponents() {
		final List<Component> all = new ArrayList<Component>();

		all.add(C(ContentFetcher.class, DefaultContentFetcher.class));
		all.add(C(ProductLineConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(MetricConfigManager.class).req(ConfigDao.class, ContentFetcher.class, ProductLineConfigManager.class));
		all.add(C(MessageAnalyzer.class, MetricAnalyzer.ID, MetricAnalyzer.class).is(PER_LOOKUP)
		//
		      .req(ReportBucketManager.class, BusinessReportDao.class, MetricConfigManager.class)//
		      .req(ProductLineConfigManager.class, TaskManager.class, ServerConfigManager.class));

		return all;
	}

	private Collection<Component> defineProblemComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = ProblemAnalyzer.ID;

		all.add(C(ProblemHandler.class, DefaultProblemHandler.ID, DefaultProblemHandler.class)//
		      .config(E("errorType").value("Error,RuntimeException,Exception"))//
		      .req(ServerConfigManager.class));

		all.add(C(ProblemHandler.class, LongExecutionProblemHandler.ID, LongExecutionProblemHandler.class) //
		      .req(ServerConfigManager.class));

		all.add(C(MessageAnalyzer.class, ID, ProblemAnalyzer.class).is(PER_LOOKUP)
		//
		      .req(ReportManager.class, ID).req(ServerConfigManager.class).req(ProblemHandler.class, //
		            new String[] { DefaultProblemHandler.ID, LongExecutionProblemHandler.ID }, "m_handlers"));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP)
		//
		      .req(ReportDelegate.class, ID)
		      //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, ProblemDelegate.class) //
		      .req(TaskManager.class, ServerFilterConfigManager.class));

		return all;
	}

	private Collection<Component> defineStateComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = StateAnalyzer.ID;

		all.add(C(ProjectService.class).req(ProjectDao.class, ServerConfigManager.class));
		all.add(C(MessageAnalyzer.class, ID, StateAnalyzer.class).is(PER_LOOKUP).req(ReportManager.class, ID)
		      .req(ServerStatisticManager.class, ServerConfigManager.class, ServerFilterConfigManager.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP)
		//
		      .req(ReportDelegate.class, ID)
		      //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, StateDelegate.class) //
		      .req(TaskManager.class, ReportBucketManager.class));

		return all;
	}

	private Collection<Component> defineTopComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = TopAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, TopAnalyzer.class).is(PER_LOOKUP).req(ReportManager.class, ID)
		      .req(ServerConfigManager.class, ServerFilterConfigManager.class)
		      .config(E("errorType").value("Error,RuntimeException,Exception")));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP)
		//
		      .req(ReportDelegate.class, ID)
		      //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, TopDelegate.class));

		return all;
	}

	Collection<Component> defineTransactionComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = TransactionAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, TransactionAnalyzer.class).is(PER_LOOKUP).req(ReportManager.class, ID)
		      .req(ReportDelegate.class, ID).req(ServerConfigManager.class, ServerFilterConfigManager.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP)
		//
		      .req(ReportDelegate.class, ID)
		      //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, TransactionDelegate.class).req(TaskManager.class,
		      ServerFilterConfigManager.class, AllReportConfigManager.class));

		return all;
	}

	private List<Component> defineReportComponents() {
		final List<Component> all = new ArrayList<Component>();

		all.add(A(TransactionReportManager.class));
		all.add(A(TransactionReportAggregator.class));
		all.add(A(TransactionReportDelegate.class));
		all.add(A(TransactionReportAnalyzer.class));

		all.add(A(TransactionReportHelper.class));
		all.add(A(TransactionTypeFilter.class));
		all.add(A(TransactionTypeGraphFilter.class));
		all.add(A(TransactionNameFilter.class));
		all.add(A(TransactionNameGraphFilter.class));

		all.add(A(EventReportManager.class));
		all.add(A(EventReportAggregator.class));
		all.add(A(EventReportDelegate.class));
		all.add(A(EventReportAnalyzer.class));
		all.add(A(EventReportFilter.class));

		return all;
	}

	private Collection<Component> defineStorageComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = StorageAnalyzer.ID;
		all.add(C(com.dianping.cat.consumer.storage.DatabaseParser.class));
		all.add(C(StorageReportUpdater.class));
		all.add(C(MessageAnalyzer.class, ID, StorageAnalyzer.class).is(PER_LOOKUP).req(ReportManager.class, ID)
		      .req(ReportDelegate.class, ID).req(ServerConfigManager.class)
		      .req(com.dianping.cat.consumer.storage.DatabaseParser.class).req(StorageReportUpdater.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP)
		//
		      .req(ReportDelegate.class, ID)
		      //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, StorageDelegate.class).req(TaskManager.class,
		      ServerFilterConfigManager.class, StorageReportUpdater.class));

		// database
		// all.add(C(JdbcDataSourceDescriptorManager.class) //
		// .config(E("datasourceFile").value("/data/appdatas/cat/datasources.xml")));

		return all;
	}
}
