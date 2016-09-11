package org.unidal.cat.message;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.message.storage.BucketTest;
import org.unidal.cat.message.storage.MessageDumperTest;

@RunWith(Suite.class)
@SuiteClasses({

BucketTest.class,

MessageDumperTest.class

})
public class AllPerfTests {

}
