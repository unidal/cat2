package org.unidal.cat.message.storage.internals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.CatConstant;
import org.unidal.cat.message.storage.MessageDumper;
import org.unidal.cat.message.storage.MessageDumperManager;
import org.unidal.cat.message.storage.hdfs.LogviewProcessor;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = MessageDumperManager.class)
public class DefaultMessageDumperManager extends ContainerHolder implements MessageDumperManager, Initializable {

	@Inject
	private LogviewProcessor m_logviewProcessor;

	private Map<Integer, MessageDumper> m_dumpers = new LinkedHashMap<Integer, MessageDumper>();

	@Override
	public void close(int hour) {
		MessageDumper dumper = m_dumpers.remove(hour);

		if (dumper != null) {
			try {
				dumper.awaitTermination(hour);
			} catch (InterruptedException e) {
				// ignore
			}
			super.release(dumper);
		}
	}

	@Override
	public MessageDumper find(int hour) {
		return m_dumpers.get(hour);
	}

	@Override
	public MessageDumper findOrCreate(int hour) {
		MessageDumper dumper = m_dumpers.get(hour);

		if (dumper == null) {
			synchronized (this) {
				dumper = m_dumpers.get(hour);

				if (dumper == null) {
					dumper = lookup(MessageDumper.class);
					dumper.initialize(hour);

					m_dumpers.put(hour, dumper);
				}
			}
		}

		return dumper;
	}

	@Override
	public void initialize() throws InitializationException {
		Threads.forGroup(CatConstant.CAT).start(m_logviewProcessor);
	}
}
