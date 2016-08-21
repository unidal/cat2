package org.unidal.cat.plugin.transaction;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.plugin.transaction.filter.TransactionReportFilterTest;
import org.unidal.cat.plugin.transaction.model.TransactionReportAggregatorTest;
import org.unidal.cat.plugin.transaction.reducer.TransactionReportReducerTest;

@RunWith(Suite.class)
@SuiteClasses({

TransactionReportAggregatorTest.class,

TransactionReportFilterTest.class,

TransactionReportReducerTest.class,

})
public class AllTests {

}
