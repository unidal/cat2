package com.dianping.cat.message.spi.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageStatistics.class)
public class DefaultMessageStatistics implements MessageStatistics {
	private AtomicLong m_produced = new AtomicLong();

	private AtomicLong m_overflowed = new AtomicLong();

	private AtomicLong m_bytes = new AtomicLong();

	@Override
	public void onBytes(int bytes) {
		m_bytes.addAndGet(bytes);
		m_produced.incrementAndGet();
	}

	@Override
	public void onOverflowed(MessageTree tree) {
		m_overflowed.incrementAndGet();
	}

	@Override
	public Map<String, Long> getStatistics() {
		Map<String, Long> map = new HashMap<String, Long>();

		map.put("cat.status.message.produced", m_produced.get());
		m_produced = new AtomicLong();
		
		map.put("cat.status.message.overflowed", m_overflowed.get());
		m_overflowed = new AtomicLong();
		
		map.put("cat.status.message.bytes", m_bytes.get());
		m_bytes = new AtomicLong();

		return map;
	}
}
