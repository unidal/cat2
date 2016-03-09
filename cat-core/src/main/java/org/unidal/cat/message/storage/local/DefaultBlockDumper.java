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

import com.dianping.cat.Cat;

@Named(type = BlockDumper.class)
public class DefaultBlockDumper extends ContainerHolder implements BlockDumper, Initializable {
	private List<BlockingQueue<Block>> m_queues = new ArrayList<BlockingQueue<Block>>();

	private List<BlockWriter> m_writers = new ArrayList<BlockWriter>();

	private int m_failCount = -1;

	@Override
	public void awaitTermination() throws InterruptedException {
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

			TimeUnit.MILLISECONDS.sleep(1);
		}

		for (BlockWriter writer : m_writers) {
			writer.shutdown();
		}
	}

	@Override
	public void dump(Block block) throws IOException {
		String domain = block.getDomain();
		int hash = Math.abs(domain.hashCode());
		int index = hash % m_writers.size();
		BlockingQueue<Block> queue = m_queues.get(index);

		boolean success = queue.offer(block);

		if (!success && (++m_failCount % 100) == 0) {
			Cat.logError(new RuntimeException("Error when offer tree in block dumper"));
		}
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
}
