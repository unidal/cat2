package org.unidal.cat.message.codec;

import org.unidal.cat.message.command.Command;

import io.netty.buffer.ByteBuf;

public interface CommandCodec {
	public Command decode(ByteBuf buf);

	public void encode(Command cmd, ByteBuf buf);
}
