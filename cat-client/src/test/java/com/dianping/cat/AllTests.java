package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.cat.message.MessageIdFactoryTest;
import org.unidal.cat.message.codec.BinaryMessageCodecTest;

import com.dianping.cat.message.EventTest;
import com.dianping.cat.message.HeartbeatTest;
import com.dianping.cat.message.MessageTest;
import com.dianping.cat.message.TransactionTest;
import com.dianping.cat.message.internal.MockMessageBuilderTest;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest;
import com.dianping.cat.servlet.CatFilterTest;

@RunWith(Suite.class)
@SuiteClasses({

/* .message */
MessageTest.class,

EventTest.class,

HeartbeatTest.class,

TransactionTest.class,

MockMessageBuilderTest.class,

/* .spi.codec */
PlainTextMessageCodecTest.class,

/* .servlet */
CatFilterTest.class,

/* .tool */
ToolsTest.class,

CatTest.class,

/* .message */

MessageIdFactoryTest.class,

BinaryMessageCodecTest.class,

})
public class AllTests {

}
