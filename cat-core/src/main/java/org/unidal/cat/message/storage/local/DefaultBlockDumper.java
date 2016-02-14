package org.unidal.cat.message.storage.local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.message.storage.Block;
import org.unidal.cat.message.storage.BlockDumper;
import org.unidal.cat.message.storage.BlockWriter;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = BlockDumper.class)
public class DefaultBlockDumper extends ContainerHolder implements BlockDumper, Initializable {
	private List<BlockingQueue<Block>> m_queues = new ArrayList<BlockingQueue<Block>>();

	private List<BlockWriter> m_writers = new ArrayList<BlockWriter>();

	@Override
	public void dump(Block block) throws IOException {
		String domain = block.getDomain();
		int hash = domain.hashCode();
		int index = hash % m_writers.size();
		BlockingQueue<Block> queue = m_queues.get(index);

		queue.offer(block);
	}

	@Override
	public void initialize() throws InitializationException {
		for (int i = 0; i < 10; i++) {
			BlockingQueue<Block> queue = new LinkedBlockingQueue<Block>(10000);
			BlockWriter writer = lookup(BlockWriter.class);

			m_queues.add(queue);
			m_writers.add(writer);

			writer.initialize(i, queue);
			Threads.forGroup("Cat").start(writer);
		}
	}

	@Override
	public void awaitTermination() throws InterruptedException {
		long t1 = System.currentTimeMillis();

		while (true) {
			boolean allEmpty = true;

			for (BlockingQueue<Block> queue : m_queues) {
				if (!queue.isEmpty()) {
					allEmpty = false;
					break;
				}
			}

			if (allEmpty) {
				break;
			}

			TimeUnit.MILLISECONDS.sleep(5);
		}

		long t2 = System.currentTimeMillis();
		System.out.println("queue: " + (t2 - t1) + " ms");

		for (BlockWriter writer : m_writers) {
			writer.shutdown();

			Thread.yield();
		}
	}
}
