package org.unidal.cat.plugin.transactions;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.plugin.transactions.config.TransactionsConfigServiceTest;
import org.unidal.cat.plugin.transactions.config.TransactionsConfigTest;
import org.unidal.cat.plugin.transactions.filter.TransactionsReportFilterTest;
import org.unidal.cat.plugin.transactions.model.TransactionsReportAggregatorTest;
import org.unidal.cat.plugin.transactions.model.TransactionsReportManagerTest;
import org.unidal.cat.plugin.transactions.reducer.TransactionsReportReducerTest;

@RunWith(Suite.class)
@SuiteClasses({

TransactionsConfigTest.class,

TransactionsConfigServiceTest.class,

TransactionsReportFilterTest.class,

TransactionsReportManagerTest.class,

TransactionsReportAggregatorTest.class,

TransactionsReportReducerTest.class,

})
public class AllTests {

}
