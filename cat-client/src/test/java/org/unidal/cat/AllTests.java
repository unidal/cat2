package org.unidal.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.config.ClientConfigurationProviderTest;
import org.unidal.cat.config.internals.ServerDiscoveryTest;

@RunWith(Suite.class)
@SuiteClasses({

ClientConfigurationProviderTest.class,

ServerDiscoveryTest.class,

})
public class AllTests {

}
