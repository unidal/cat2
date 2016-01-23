package org.unidal.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.plugin.transaction.AllTransactionTests;
import org.unidal.cat.service.CompressionServiceTest;
import org.unidal.cat.spi.ReportFilterManagerTest;
import org.unidal.cat.spi.ReportManagerManagerTest;
import org.unidal.cat.spi.ReportPeriodTest;
import org.unidal.cat.spi.remote.RemoteIntegrationTest;
import org.unidal.cat.spi.report.provider.ReportProviderTest;
import org.unidal.cat.spi.report.storage.ReportStorageTest;

@RunWith(Suite.class)
@SuiteClasses({

/** service **/
CompressionServiceTest.class,

/** report **/
ReportFilterManagerTest.class,

ReportManagerManagerTest.class,

ReportPeriodTest.class,

ReportProviderTest.class,

ReportStorageTest.class,

RemoteIntegrationTest.class,

/** plugin **/
AllTransactionTests.class

})
public class AllCat2Tests {

}
