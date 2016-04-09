package org.unidal.cat.message;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.message.storage.IndexManagerTest;
import org.unidal.cat.message.storage.IndexTest;
import org.unidal.cat.message.storage.TokenMappingTest;
import org.unidal.cat.spi.analysis.MessageAnalyzerManagerTest;
import org.unidal.cat.spi.analysis.MessageDispatcherTest;
import org.unidal.cat.spi.analysis.event.TimeWindowManagerTest;

@RunWith(Suite.class)
@SuiteClasses({

MessageIdTest.class,

/* .storage */

IndexManagerTest.class,

IndexTest.class,

TokenMappingTest.class,

IndexTest.class,

/* .analysis */

MessageAnalyzerManagerTest.class,

MessageDispatcherTest.class,

TimeWindowManagerTest.class

})
public class AllMessageTests {

}
