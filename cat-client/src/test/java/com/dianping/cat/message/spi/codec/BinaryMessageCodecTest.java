package com.dianping.cat.message.spi.codec;

import io.netty.buffer.ByteBuf;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;

public class BinaryMessageCodecTest extends ComponentTestCase {

	@Test
	public void testBinary() {
		MessageCodec nativeCodec = lookup(MessageCodec.class, NativeMessageCodec.ID);
		MessageTree expected = TreeHelper.tree(new MessageId("test", "7f000001", 405638, 0));
		ByteBuf buf = nativeCodec.encode(expected);
		MessageTree actual = nativeCodec.decode(buf);

		String a = PlainTextMessageCodec.encodeTree(expected);
		String b = PlainTextMessageCodec.encodeTree(actual);

		Assert.assertEquals(a, b);

		MessageCodec plainCodec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
		Assert.assertEquals(plainCodec.encode(expected), plainCodec.encode(actual));
	}
	
	@Test
	public void testAtomicBinary() {
		MessageCodec nativeCodec = lookup(MessageCodec.class, NativeMessageCodec.ID);
		MessageTree expected = TreeHelper.atomicTree(new MessageId("test", "7f000001", 405638, 0));
		ByteBuf buf = nativeCodec.encode(expected);
		MessageTree actual = nativeCodec.decode(buf);

		String a = PlainTextMessageCodec.encodeTree(expected);
		String b = PlainTextMessageCodec.encodeTree(actual);

		Assert.assertEquals(a, b);

		MessageCodec plainCodec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
		Assert.assertEquals(plainCodec.encode(expected), plainCodec.encode(actual));
	}

	@Test
	public void testPlainText() {
		MessageCodec codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
		MessageTree expected = TreeHelper.tree(new MessageId("test", "7f000001", 405638, 0));
		ByteBuf buf = codec.encode(expected);

		MessageTree actual = codec.decode(buf);

		System.out.println(actual.getTransactions().size());
		System.out.println(actual.getEvents().size());
		System.out.println(actual.getMetrics().size());

		Assert.assertEquals(codec.encode(expected), codec.encode(actual));
	}
}
