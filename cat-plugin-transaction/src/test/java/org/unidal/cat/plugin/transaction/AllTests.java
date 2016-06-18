package org.unidal.cat.plugin.transaction;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.plugin.transaction.filter.TransactionReportFilterTest;

@RunWith(Suite.class)
@SuiteClasses({

TransactionReportManagerTest.class,

TransactionReportAggregatorTest.class,

TransactionReportFilterTest.class

})
public class AllTests {

}
