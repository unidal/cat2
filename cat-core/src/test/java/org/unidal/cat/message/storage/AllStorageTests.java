package org.unidal.cat.message.storage;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({

MessageIdTest.class,

IndexManagerTest.class,

TokenMappingTest.class,

IndexTest.class,

})
public class AllStorageTests {

}
