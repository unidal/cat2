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

@Named(type = MessageDumperManager.class)
public class MessageDumperManager extends ContainerHolder implements LogEnabled {
	private Map<Integer, MessageDumper> m_dumpers = new LinkedHashMap<Integer, MessageDumper>();

	private Logger m_logger;

	public void closeDumper(int hour) {
		MessageDumper dumper = m_dumpers.remove(hour);

		if (dumper != null) {
			try {
				dumper.awaitTermination(hour);
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public MessageDumper findDumper(int hour) {
		return m_dumpers.get(hour);
	}

	public MessageDumper findOrCreateMessageDumper(int hour) {
		MessageDumper dumper = m_dumpers.get(hour);

		if (dumper == null) {
			synchronized (this) {
				dumper = m_dumpers.get(hour);

				if (dumper == null) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

					dumper = lookup(MessageDumper.class);
					dumper.initialize(hour);

					m_dumpers.put(hour, dumper);
					m_logger.info("create message dumper " + sdf.format(new Date(TimeUnit.HOURS.toMillis(hour))));
				}
			}
		}

		return dumper;
	}

}
