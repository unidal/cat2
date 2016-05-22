package org.unidal.cat.command;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import junit.framework.Assert;

import org.junit.Test;
import org.unidal.cat.message.codec.CommandCodec;
import org.unidal.cat.message.codec.NativeCommandCodec;
import org.unidal.cat.message.command.Command;
import org.unidal.cat.message.command.DefaultCommand;
import org.unidal.lookup.ComponentTestCase;

public class CommandCodecTest extends ComponentTestCase {
	@Test
	public void test() {
		CommandCodec codec = lookup(CommandCodec.class, NativeCommandCodec.ID);
		ByteBuf buf = Unpooled.buffer();
		Command cmd = new DefaultCommand("mock", 1460617463626L);

		codec.encode(cmd, buf);
		Command actual = codec.decode(buf);

		Assert.assertEquals(cmd.toString(), actual.toString());
	}
}
