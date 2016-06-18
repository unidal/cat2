package org.unidal.cat.transport.decode;

import io.netty.buffer.ByteBuf;

public interface DecodeHandler {
	public void handle(ByteBuf buf);
}
