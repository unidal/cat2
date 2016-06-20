package org.unidal.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.spi.ReportManagerManagerTest;
import org.unidal.cat.spi.ReportPeriodTest;
import org.unidal.cat.spi.remote.RemoteIntegrationTest;
import org.unidal.cat.spi.report.ReportFilterManagerTest;

@RunWith(Suite.class)
@SuiteClasses({

/** report **/
ReportFilterManagerTest.class,

ReportManagerManagerTest.class,

ReportPeriodTest.class,

RemoteIntegrationTest.class,

})
public class AllCat2Tests {

}
