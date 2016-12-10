package com.dianping.cat.analyzer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class EventAggregator {

	private static ConcurrentHashMap<String, EventData> s_events = new ConcurrentHashMap<String, EventData>();

	private static String buildKey(String type, String name) {
		return type + ',' + name;
	}

	private static EventData createEventData(String type, String name) {
		return new EventData(type, name);
	}

	public static Map<String, EventData> getAndResetEvents() {
		Map<String, EventData> cloned = s_events;

		synchronized (EventAggregator.class) {
			s_events = new ConcurrentHashMap<String, EventData>();

			for (Map.Entry<String, EventData> entry : cloned.entrySet()) {
				String key = entry.getKey();
				EventData data = entry.getValue();

				if (s_events.size() < 2000 && data.getCount() > 0) {
					s_events.put(key, createEventData(data.getType(), data.getName()));
				}
			}
		}
		return cloned;
	}

	public static void logEvent(Event e) {
		makeSureEventExist(e).add(e);
	}

	private static EventData makeSureEventExist(Event event) {
		String key = buildKey(event.getType(), event.getName());
		EventData item = s_events.get(key);

		if (null == item) {
			synchronized (EventAggregator.class) {
				item = s_events.get(key);
				if (null == item) {
					item = createEventData(event.getType(), event.getName());

					s_events.put(key, item);
				}
			}
		}
		return item;
	}

	public static void sendEventData() {
		Map<String, EventData> events = getAndResetEvents();

		if (events.size() > 0) {
			Transaction t = Cat.newTransaction("_CatMergeTree", "_CatMergeTree");
			Cat.getManager().getThreadLocalMessageTree().setDiscard(false);

			for (EventData data : events.values()) {
				if (data.getCount() > 0) {
					Event tmp = Cat.newEvent(data.getType(), data.getName());
					StringBuilder sb = new StringBuilder(32);

					sb.append(CatConstants.BATCH_FLAG).append(data.getCount()).append(CatConstants.SPLIT)
					      .append(data.getError());
					tmp.addData(sb.toString());
					tmp.setStatus(Message.SUCCESS);
					tmp.complete();
				}
			}

			t.setStatus(Message.SUCCESS);
			t.complete();
		}
	}

	public static class EventData {
		private String m_key;

		private String m_type;

		private String m_name;

		private AtomicInteger m_count = new AtomicInteger();

		private AtomicInteger m_error = new AtomicInteger();

		public EventData(String type, String name) {
			m_type = type;
			m_name = name;
			m_key = buildKey(type, name);
		}

		public EventData add(Event e) {
			m_count.incrementAndGet();

			if (!e.isSuccess()) {
				m_error.incrementAndGet();
			}
			return this;
		}

		public int getCount() {
			return m_count.get();
		}

		public int getError() {
			return m_error.get();
		}

		public String getKey() {
			return m_key;
		}

		public String getName() {
			return m_name;
		}

		public String getType() {
			return m_type;
		}
	}

}
