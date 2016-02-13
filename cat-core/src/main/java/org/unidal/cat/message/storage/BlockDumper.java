package org.unidal.cat.message.storage;

import java.io.IOException;

public interface BlockDumper {
	public void dump(Block block) throws IOException;
}
