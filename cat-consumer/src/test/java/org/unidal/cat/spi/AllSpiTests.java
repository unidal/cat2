package org.unidal.cat.spi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.plugin.transaction.AllTransactionTests;
import org.unidal.cat.spi.remote.RemoteIntegrationTest;
import org.unidal.cat.spi.report.provider.ReportProviderTest;
import org.unidal.cat.spi.report.storage.ReportStorageTest;

@RunWith(Suite.class)
@SuiteClasses({

ReportFilterManagerTest.class,

ReportManagerManagerTest.class,

ReportPeriodTest.class,

ReportProviderTest.class,

ReportStorageTest.class,

RemoteIntegrationTest.class,

AllTransactionTests.class

})
public class AllSpiTests {

}
