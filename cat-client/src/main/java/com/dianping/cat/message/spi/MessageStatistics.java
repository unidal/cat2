package com.dianping.cat.message.spi;

import java.util.Map;

public interface MessageStatistics {
	
	public Map<String,Long> getStatistics();

	public void onBytes(int size);

	public void onOverflowed(MessageTree tree);

}
