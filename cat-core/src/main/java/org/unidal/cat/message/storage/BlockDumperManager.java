package org.unidal.cat.message.storage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = BlockDumperManager.class)
public class BlockDumperManager extends ContainerHolder implements LogEnabled {
	private Map<Integer, BlockDumper> m_dumpers = new LinkedHashMap<Integer, BlockDumper>();

	private Logger m_logger;

	public void closeDumper(int hour) {
		BlockDumper dumper = m_dumpers.remove(hour);

		if (dumper != null) {
			try {
				dumper.awaitTermination();
			} catch (InterruptedException e) {
				// ignore it
			}
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public BlockDumper findDumper(int hour) {
		return m_dumpers.get(hour);
	}

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
