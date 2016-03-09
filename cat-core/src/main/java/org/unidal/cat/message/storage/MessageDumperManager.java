package org.unidal.cat.message.storage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = MessageDumperManager.class)
public class MessageDumperManager extends ContainerHolder implements LogEnabled {

	private Map<Long, MessageDumper> m_dumpers = new LinkedHashMap<Long, MessageDumper>();

	private Logger m_logger;

	public void closeDumper(long timestamp) {
		MessageDumper dumper = m_dumpers.get(timestamp);

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

	public MessageDumper findDumper(long timestamp) {
		return m_dumpers.get(timestamp);
	}

	public MessageDumper findOrCreateMessageDumper(long timestamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		MessageDumper dumper = m_dumpers.get(timestamp);

		if (dumper == null) {
			synchronized (this) {
				dumper = m_dumpers.get(timestamp);
				if (dumper == null) {
					dumper = lookup(MessageDumper.class);

					m_dumpers.put(timestamp, dumper);
					m_logger.info("create message dumper " + sdf.format(new Date(timestamp)));
				}
			}
		}
		return dumper;
	}

}
