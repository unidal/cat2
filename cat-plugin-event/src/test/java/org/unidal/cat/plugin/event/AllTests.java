package org.unidal.cat.plugin.event;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.plugin.event.filter.EventReportFilterTest;
import org.unidal.cat.plugin.event.model.EventReportAggregatorTest;
import org.unidal.cat.plugin.event.reducer.EventReportReducerTest;

@RunWith(Suite.class)
@SuiteClasses({

EventReportAggregatorTest.class,

EventReportFilterTest.class,

EventReportReducerTest.class,

})
public class AllTests {

}
