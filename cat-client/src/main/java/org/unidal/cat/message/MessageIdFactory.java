package org.unidal.cat.message;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileLock;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.unidal.helper.Inets;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.internal.MilliSecondTimer;

@Named
public class MessageIdFactory {
	private static final long HOUR = 3600 * 1000L;

	private String m_domain;

	private String m_ipAddress;

	private long m_lastTimestamp;

	private AtomicInteger m_batchStart;

	private AtomicInteger m_batchOffset;

	private RandomAccessFile m_markFile;

	private MappedByteBuffer m_byteBuffer;

	public void initialize(File baseDir, String domain) throws IOException {
		File file = new File(baseDir, domain + ".mark");

		file.getParentFile().mkdirs();

		m_domain = domain;
		m_markFile = new RandomAccessFile(file, "rw");
		m_byteBuffer = m_markFile.getChannel().map(MapMode.READ_WRITE, 0, 20);
		m_batchStart = new AtomicInteger();
		m_batchOffset = new AtomicInteger();
	}

	protected int getBatchSize() {
		return 100;
	}

	protected String getDomain() {
		return m_domain;
	}

	protected synchronized int getIndex(long timestamp) {
		if (m_domain == null) {
			throw new IllegalStateException("MessageIdFactory is not initialized, call method initialize(...) first!");
		}

		int offset = m_batchOffset.incrementAndGet();

		if (m_lastTimestamp != timestamp || offset >= getBatchSize()) {
			FileLock lock = null;

			try {
				lock = lock();

				int start = m_byteBuffer.limit() >= 4 ? m_byteBuffer.getInt(0) : 0;
				long lastTimestamp = m_byteBuffer.limit() >= 12 ? m_byteBuffer.getLong(4) : 0;

				if (lastTimestamp == timestamp) { // same hour
					m_batchStart.set(start);
				} else {
					m_batchStart.set(0);
				}

				offset = 0;
				m_batchOffset.set(0);
				m_lastTimestamp = timestamp;
				m_byteBuffer.putInt(0, m_batchStart.get() + getBatchSize());
				m_byteBuffer.putLong(4, timestamp);
				m_markFile.getChannel().force(false);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (lock != null) {
					try {
						lock.release();
					} catch (IOException e) {
						// ignore it
					}
				}
			}
		}

		return m_batchStart.get() + offset;
	}

	protected String getIpAddress() {
		if (m_ipAddress == null) {
			String ip = Inets.IP4.getLocalHostAddress();
			List<String> items = Splitters.by(".").noEmptyItem().split(ip);

			if (items.size() == 4) {
				byte[] bytes = new byte[4];

				for (int i = 0; i < 4; i++) {
					bytes[i] = (byte) Integer.parseInt(items.get(i));
				}

				StringBuilder sb = new StringBuilder(bytes.length / 2);

				for (byte b : bytes) {
					sb.append(Integer.toHexString((b >> 4) & 0x0F));
					sb.append(Integer.toHexString(b & 0x0F));
				}

				m_ipAddress = sb.toString();
			} else {
				System.out.println("[ERROR] Unrecognized IP: " + ip + "!");

				m_ipAddress = "7f000001";
			}
		}

		return m_ipAddress;
	}

	public String getNextId() {
		long timestamp = getTimestamp();
		int index = getIndex(timestamp);
		StringBuilder sb = new StringBuilder(m_domain.length() + 32);

		sb.append(getDomain());
		sb.append('-');
		sb.append(getIpAddress());
		sb.append('-');
		sb.append(timestamp);
		sb.append('-');
		sb.append(index);

		return sb.toString();
	}

	protected long getTimestamp() {
		long timestamp = MilliSecondTimer.currentTimeMillis();

		return timestamp / HOUR;
	}

	private FileLock lock() throws InterruptedException {
		FileLock lock = null;

		while (lock == null) {
			try {
				lock = m_markFile.getChannel().tryLock();
			} catch (Exception e) {
				// ignore it
			}

			if (lock == null) {
				TimeUnit.MILLISECONDS.sleep(1);
			}
		}

		return lock;
	}
}
