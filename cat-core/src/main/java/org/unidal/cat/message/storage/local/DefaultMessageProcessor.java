package org.unidal.cat.message.storage.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unidal.cat.message.MessageId;
import org.unidal.cat.message.storage.Block;
import org.unidal.cat.message.storage.BlockDumper;
import org.unidal.cat.message.storage.MessageProcessor;
import org.unidal.cat.message.storage.internals.DefaultBlock;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

@Named(type = MessageProcessor.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultMessageProcessor implements MessageProcessor {
	@Inject
	private BlockDumper m_dumper;

	private int m_index;

	private BlockingQueue<MessageTree> m_queue;

	private Map<String, Block> m_blocks = new HashMap<String, Block>();

	private AtomicBoolean m_enabled;

	@Override
	public String getName() {
		return getClass() + "-" + m_index;
	}

	@Override
	public void shutdown() {
		m_enabled.set(false);

		for (Block block : m_blocks.values()) {
			try {
				m_dumper.dump(block);
			} catch (IOException e) {
				// ignore it
			}
		}

		m_blocks.clear();
	}

	@Override
	public void run() {
		try {
			while (m_enabled.get()) {
				DefaultMessageTree tree = (DefaultMessageTree) m_queue.poll(5, TimeUnit.MILLISECONDS);

				if (tree != null) {
					MessageId id = MessageId.parse(tree.getMessageId());
					String domain = id.getDomain();
					int hour = id.getHour();
					Block block = m_blocks.get(domain);

					if (block == null) {
						block = new DefaultBlock(domain, hour);
						m_blocks.put(domain, block);
					}

					try {
						block.pack(id, tree.getBuffer());

						if (block.isFull()) {
							block.finish();

							m_dumper.dump(block);
							m_blocks.put(domain, new DefaultBlock(domain, hour));
						}
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
	public void initialize(int index, BlockingQueue<MessageTree> queue) {
		m_index = index;
		m_queue = queue;
		m_enabled = new AtomicBoolean(true);
	}
}
