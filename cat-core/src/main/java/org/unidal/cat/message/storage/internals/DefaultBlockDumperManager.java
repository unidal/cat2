package org.unidal.cat.message.storage.internals;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.message.storage.BlockDumper;
import org.unidal.cat.message.storage.BlockDumperManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = BlockDumperManager.class)
public class DefaultBlockDumperManager extends ContainerHolder implements LogEnabled, BlockDumperManager {
	private Map<Integer, BlockDumper> m_dumpers = new LinkedHashMap<Integer, BlockDumper>();

	private Logger m_logger;

	@Override
	public void closeDumper(int hour) {
		BlockDumper dumper = m_dumpers.remove(hour);

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
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public BlockDumper findDumper(int hour) {
		return m_dumpers.get(hour);
	}

	@Override
	public BlockDumper findOrCreateBlockDumper(int hour) {
		BlockDumper dumper = m_dumpers.get(hour);

		if (dumper == null) {
			synchronized (this) {
				dumper = m_dumpers.get(hour);

				if (dumper == null) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					dumper = lookup(BlockDumper.class);
					dumper.initialize(hour);

					m_dumpers.put(hour, dumper);
					m_logger.info("Create block dumper " + sdf.format(new Date(TimeUnit.HOURS.toMillis(hour))));
				}
			}
		}

		return dumper;
	}
}
