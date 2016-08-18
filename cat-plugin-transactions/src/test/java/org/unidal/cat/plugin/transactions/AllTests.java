package org.unidal.cat.plugin.transactions;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.plugin.transactions.config.TransactionsConfigTest;

@RunWith(Suite.class)
@SuiteClasses({

TransactionsConfigTest.class,

TransactionsReportManagerTest.class,

TransactionsReportAggregatorTest.class,

})
public class AllTests {

}
