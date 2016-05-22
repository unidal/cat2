package org.unidal.cat.message.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.Test;
import org.unidal.cat.codec.NativeMessageCodec;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

public class MessageCodecPerfTest extends ComponentTestCase {
	@Test
	public void compares() {
		String[] types = { PlainTextMessageCodec.ID, NativeMessageCodec.ID, PlainTextMessageCodec.ID,
		      NativeMessageCodec.ID, PlainTextMessageCodec.ID, NativeMessageCodec.ID };
		int len = types.length;
		MessageCodec[] codecs = new MessageCodec[len];

		for (int i = 0; i < len; i++) {
			codecs[i] = lookup(MessageCodec.class, types[i]);
		}

		MessageTree tree = TreeHelper.tree(new MessageId("test", "7f000001", 405638, 0));
		int times = 1000;

		for (int i = 0; i < len; i++) {
			MessageCodec codec = codecs[i];

			// warm up
			int size = doCodec(tree, codec);
			long start = System.currentTimeMillis();

			for (int j = 0; j < times; j++) {
				doCodec(tree, codec);
			}

			long end = System.currentTimeMillis();

			long each = (end - start) * 1000L / times;
			System.out.println(String.format("%s: %sms, each: %sus, size: %s", types[i], end - start, each, size));
		}
	}

	private int doCodec(MessageTree tree, MessageCodec codec) {
		int size;
		ByteBuf buf = Unpooled.buffer(2 * 1024);

		codec.encode(tree, buf);

		size = buf.readableBytes();

		codec.decode(buf);
		return size;
	}
}
