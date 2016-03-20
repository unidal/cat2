package org.unidal.cat.message.storage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = BlockDumperManager.class)
public class BlockDumperManager extends ContainerHolder implements LogEnabled {

	private Map<Long, BlockDumper> m_dumpers = new LinkedHashMap<Long, BlockDumper>();

	private Logger m_logger;

	public void closeDumper(long timestamp) {
		BlockDumper dumper = m_dumpers.get(timestamp);

		if (dumper != null) {
			try {
				dumper.awaitTermination();
			} catch (InterruptedException e) {
				// ingore
			}
		}
		m_dumpers.remove(timestamp);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public BlockDumper findDumper(long timestamp) {
		return m_dumpers.get(timestamp);
	}

	public BlockDumper findOrCreateBlockDumper(long timestamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		BlockDumper dumper = m_dumpers.get(timestamp);

		if (dumper == null) {
			synchronized (this) {
				dumper = m_dumpers.get(timestamp);
				if (dumper == null) {
					dumper = lookup(BlockDumper.class);
					dumper.initialize(timestamp);

					m_dumpers.put(timestamp, dumper);
					m_logger.info("create block dumper " + sdf.format(new Date(timestamp)));
				}
			}
		}
		return dumper;
	}

}
