package org.unidal.cat.plugin.event;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.plugin.event.config.EventConfigServiceTest;
import org.unidal.cat.plugin.event.config.EventConfigTest;
import org.unidal.cat.plugin.event.filter.EventReportFilterTest;
import org.unidal.cat.plugin.event.model.EventReportAggregatorTest;
import org.unidal.cat.plugin.event.reducer.EventReportReducerTest;
import org.unidal.cat.plugin.event.report.page.PayloadTest;

@RunWith(Suite.class)
@SuiteClasses({

EventConfigServiceTest.class,

EventConfigTest.class,

EventReportAggregatorTest.class,

EventReportFilterTest.class,

EventReportReducerTest.class,

PayloadTest.class,

})
public class AllTests {

}
