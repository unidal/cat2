package org.unidal.cat.report;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.plugin.event.EventReportAggregatorTest;
import org.unidal.cat.plugin.transaction.TransactionReportAggregatorTest;
import org.unidal.cat.report.internals.ReportProviderTest;
import org.unidal.cat.report.internals.ReportStorageTest;
import org.unidal.cat.report.spi.remote.RemoteIntegrationTest;

@RunWith(Suite.class)
@SuiteClasses({

ReportFilterManagerTest.class,

ReportManagerManagerTest.class,

ReportPeriodTest.class,

ReportProviderTest.class,

ReportStorageTest.class,

RemoteIntegrationTest.class,

TransactionReportAggregatorTest.class,

EventReportAggregatorTest.class

})
public class AllReportTests {

}
