package org.unidal.cat.message.storage.internals;

import java.nio.ByteBuffer;

public interface ByteBufPool {

	public ByteBuffer get();

	public void put(ByteBuffer buf);

}
