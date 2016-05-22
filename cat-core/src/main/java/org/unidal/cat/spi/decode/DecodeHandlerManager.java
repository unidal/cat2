package org.unidal.cat.spi.decode;

import io.netty.buffer.ByteBuf;

public interface DecodeHandlerManager {
	public DecodeHandler getHandler(ByteBuf buf);
}
