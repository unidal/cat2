package org.unidal.cat.message.storage.internals;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Named;

@Named(type = ByteBufPool.class)
public class DefaultByteBufPool implements ByteBufPool, LogEnabled {

	private BlockingQueue<ByteBuffer> m_bufs = new ArrayBlockingQueue<ByteBuffer>(8000);

	private AtomicInteger m_counts = new AtomicInteger(0);

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public ByteBuffer get() {
		ByteBuffer buf = m_bufs.poll();

		if (buf == null) {
			if (m_counts.incrementAndGet() % 100 == 0) {
				m_logger.info("create buf:" + m_counts.get());
			}
			buf = ByteBuffer.allocate(32 * 1024);
		}

		return buf;
	}

	public void put(ByteBuffer buf) {
		byte[] array = buf.array();

		for (int i = 0; i < array.length; i++) {
			array[i] = 0;
		}

		buf.clear();

		boolean result = m_bufs.offer(buf);

		if (!result) {
			m_logger.info("error when put back buf");
		}
	}

}
