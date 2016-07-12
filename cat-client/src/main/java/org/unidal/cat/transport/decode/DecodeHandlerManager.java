package org.unidal.cat.transport.decode;

import io.netty.buffer.ByteBuf;

public interface DecodeHandlerManager {
	public DecodeHandler getHandler(ByteBuf buf);
}
