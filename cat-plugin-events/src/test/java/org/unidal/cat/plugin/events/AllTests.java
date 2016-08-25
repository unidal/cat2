package org.unidal.cat.plugin.events;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.plugin.events.config.EventsConfigServiceTest;
import org.unidal.cat.plugin.events.config.EventsConfigTest;
import org.unidal.cat.plugin.events.filter.EventsReportFilterTest;
import org.unidal.cat.plugin.events.model.EventsReportAggregatorTest;
import org.unidal.cat.plugin.events.model.EventsReportManagerTest;
import org.unidal.cat.plugin.events.reducer.EventsReportReducerTest;

@RunWith(Suite.class)
@SuiteClasses({

EventsConfigTest.class,

EventsConfigServiceTest.class,

EventsReportFilterTest.class,

EventsReportManagerTest.class,

EventsReportAggregatorTest.class,

EventsReportReducerTest.class,

})
public class AllTests {

}
