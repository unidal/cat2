package org.unidal.cat.message.storage.local;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unidal.cat.message.storage.Block;
import org.unidal.cat.message.storage.BlockWriter;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = BlockWriter.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultBlockWriter implements BlockWriter {
	@Inject
	private BucketManager m_manager;

	private int m_index;

	private BlockingQueue<Block> m_queue;

	private AtomicBoolean m_enabled;

	@Override
	public String getName() {
		return getClass().getSimpleName() + "-" + m_index;
	}

	@Override
	public void shutdown() {
		m_enabled.set(false);
	}

	@Override
	public void run() {
		try {
			while (m_enabled.get()) {
				Block block = m_queue.poll(5, TimeUnit.MILLISECONDS);

				if (block != null) {
					try {
						Bucket bucket = m_manager.getBucket(block.getDomain(), block.getHour(), true);

						bucket.put(block);
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		} catch (InterruptedException e) {
			// ignore it
		}
	}

	@Override
	public void initialize(int index, BlockingQueue<Block> queue) {
		m_index = index;
		m_queue = queue;
		m_enabled = new AtomicBoolean(true);
	}
}
