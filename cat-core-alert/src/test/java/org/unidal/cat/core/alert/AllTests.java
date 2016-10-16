package org.unidal.cat.core.alert;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.core.alert.rule.AlertRulesTest;
import org.unidal.cat.core.alert.service.AlertReportServiceTest;

@RunWith(Suite.class)
@SuiteClasses({

AlertRulesTest.class,

AlertReportServiceTest.class,

})
public class AllTests {

}
