package org.unidal.cat.core.config;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.core.config.service.DomainGroupConfigServiceTest;
import org.unidal.cat.core.config.service.DomainOrgConfigServiceTest;

@RunWith(Suite.class)
@SuiteClasses({

DomainGroupConfigServiceTest.class,

DomainOrgConfigServiceTest.class,

})
public class AllTests {

}
