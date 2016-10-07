package org.unidal.cat.core.report;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.core.report.remote.RemoteReportTest;
import org.unidal.cat.spi.ReportPeriodTest;
import org.unidal.cat.spi.analysis.event.TimeWindowManagerTest;
import org.unidal.cat.spi.report.ReportFilterManagerTest;
import org.unidal.cat.spi.report.ReportManagerManagerTest;
import org.unidal.cat.spi.report.ReportReducerManagerTest;

@RunWith(Suite.class)
@SuiteClasses({

TimeWindowManagerTest.class,

RemoteReportTest.class,

ReportFilterManagerTest.class,

ReportReducerManagerTest.class,

ReportManagerManagerTest.class,

ReportPeriodTest.class,

})
public class AllTests {

}
