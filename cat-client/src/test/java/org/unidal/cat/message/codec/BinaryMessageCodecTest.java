package org.unidal.cat.message.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.codec.NativeMessageCodec;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;

public class BinaryMessageCodecTest extends ComponentTestCase {
	@Test
	public void testBinary() {
		MessageCodec codec = lookup(MessageCodec.class, NativeMessageCodec.ID);
		MessageTree expected = TreeHelper.tree(new MessageId("test", "7f000001", 405638, 0));
		ByteBuf buf = Unpooled.buffer();

		codec.encode(expected, buf);
		MessageTree actual = codec.decode(buf);

		Assert.assertEquals(expected.toString(), actual.toString());
	}
}
