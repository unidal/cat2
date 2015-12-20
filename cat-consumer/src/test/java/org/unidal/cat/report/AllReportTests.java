package org.unidal.cat.report;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.report.internals.ReportProviderTest;
import org.unidal.cat.report.internals.ReportStorageTest;
import org.unidal.cat.report.spi.remote.RemoteIntegrationTest;
import org.unidal.cat.transaction.report.TransactionReportAggregatorTest;

@RunWith(Suite.class)
@SuiteClasses({

ReportFilterManagerTest.class,

ReportManagerManagerTest.class,

ReportPeriodTest.class,

ReportProviderTest.class,

ReportStorageTest.class,

RemoteIntegrationTest.class,

TransactionReportAggregatorTest.class

})
public class AllReportTests {

}
