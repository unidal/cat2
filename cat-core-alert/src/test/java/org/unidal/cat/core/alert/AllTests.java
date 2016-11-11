package org.unidal.cat.core.alert;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.core.alert.data.AlertDataTest;
import org.unidal.cat.core.alert.model.AlertReportServiceTest;
import org.unidal.cat.core.alert.rule.AlertRuleTest;

@RunWith(Suite.class)
@SuiteClasses({

AlertDataTest.class,

AlertRuleTest.class,

AlertReportServiceTest.class,

})
public class AllTests {

}
