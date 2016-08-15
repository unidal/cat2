package org.unidal.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.core.config.DomainGroupConfigTest;
import org.unidal.cat.core.config.DomainOrgConfigTest;

@RunWith(Suite.class)
@SuiteClasses({

DomainGroupConfigTest.class,

DomainOrgConfigTest.class,

})
public class AllTests {

}
