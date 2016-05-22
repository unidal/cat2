package org.unidal.cat.spi.decode;

import io.netty.buffer.ByteBuf;

public interface DecodeHandler {
	public void handle(ByteBuf buf);
}
