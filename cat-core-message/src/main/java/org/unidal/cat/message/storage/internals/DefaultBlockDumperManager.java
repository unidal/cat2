package org.unidal.cat.message.storage.internals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.unidal.cat.message.storage.BlockDumper;
import org.unidal.cat.message.storage.BlockDumperManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = BlockDumperManager.class)
public class DefaultBlockDumperManager extends ContainerHolder implements BlockDumperManager {
	private Map<Integer, BlockDumper> m_map = new LinkedHashMap<Integer, BlockDumper>();

	@Override
	public void close(int hour) {
		BlockDumper dumper = m_map.remove(hour);

		if (dumper != null) {
			try {
				dumper.awaitTermination();
				super.release(dumper);
			} catch (InterruptedException e) {
				// ignore it
			}
		}
	}

	@Override
	public BlockDumper findOrCreate(int hour) {
		BlockDumper dumper = m_map.get(hour);

		if (dumper == null) {
			synchronized (this) {
				dumper = m_map.get(hour);

				if (dumper == null) {
					dumper = lookup(BlockDumper.class);
					dumper.initialize(hour);

					m_map.put(hour, dumper);
				}
			}
		}

		return dumper;
	}
}
