package com.dianping.cat.status.send;

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;

public class HttpAgentSender {

	private static final int CONNECT_TIMEOUT = 1000;

	private static final int MAX_SEND_ITEM_PER_WRITE = 1024;

	private static final int SOCKET_TIMEOUT = 1000;

	private long m_interval = 60 * 1000;

	private BlockingQueue<Item> m_queue = new LinkedBlockingQueue<Item>(8192);

	private final HttpSendConfig m_config;

	private Thread m_thread;

	private volatile boolean m_running = true;

	private int m_errorCount;

	private static HttpAgentSender m_sender;

	public synchronized static HttpAgentSender getInstance(HttpSendConfig config) {
		if (m_sender == null) {
			m_sender = new HttpAgentSender(config);
		}
		return m_sender;
	}

	private HttpAgentSender(HttpSendConfig config) {
		m_config = config;
	}

	public void asyncSend(String key, String value, long mills) {
		boolean r = m_queue.offer(new Item(key, value, mills));

		if (r == false) {
			if (m_errorCount++ % 1000 == 0) {
				Cat.logError(new SendLocalAgentException("error when offer metric to http agent queue , queue is full"));
			}

			for (int i = 0; i < 1024 * 4; i++) {
				m_queue.poll(); // discard old data
			}
		}
	}

	private String buildPostParam(List<Item> list) {
		StringBuilder data = new StringBuilder(1024);
		String tag = m_config.getTag();
		int step = m_config.getStep();

		data.append("[");

		for (Item item : list) {
			String key = item.m_key;
			if (StringUtils.isEmpty(key)) {
				continue;
			}
			data.append("{");
			data.append("\"endpoint\":\"").append(m_config.getEndpoint()).append("\",");
			data.append("\"metric\":\"").append(key).append("\",");
			data.append("\"value\":\"").append(item.m_value).append("\",");
			data.append("\"timestamp\":").append(item.m_mills / 1000).append(",");
			data.append("\"step\":").append(step).append(",");
			data.append("\"counterType\":\"").append("GAUGE").append("\",");
			data.append("\"tags\":\"").append(tag).append("\"");
			data.append("},");
		}
		data.deleteCharAt(data.length() - 1);
		data.append("]\n");
		return data.toString();
	}

	private boolean doHttpPost(List<Item> list) {
		URL url = null;
		PrintWriter out = null;
		boolean r = true;
		HttpURLConnection conn = null;

		try {
			url = new URL(m_config.getUrl());
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			conn.setReadTimeout(SOCKET_TIMEOUT);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.connect();
			out = new PrintWriter(conn.getOutputStream());

			String body = buildPostParam(list);
			out.write(body);
			out.flush();

			int code = conn.getResponseCode();

			if (code > 300) {
				Cat.logError(new SendLocalAgentException("error when send local agent with code " + code));
			}
		} catch (Exception e) {
			Cat.logEvent("UnknownHttpAgent", NetworkInterfaceManager.INSTANCE.getLocalHostAddress());

			r = false;
		} finally {
			if (out != null) {
				out.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}

		if (!r) {
			for (Item item : list) {
				m_queue.offer(item);
			}
		}
		return r;
	}

	public void initWorkThread() {
		m_thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (m_running && !Thread.currentThread().isInterrupted()) {
						long start = System.currentTimeMillis();

						if (null != m_queue.peek()) {
							try {
								List<Item> list = new ArrayList<Item>();
								
								for (int i = 0; i < m_config.getMaxItemHold(); i++) {
									Item item = m_queue.poll();

									if (null == item) {
										break;
									}

									list.add(item);

									if (list.size() >= MAX_SEND_ITEM_PER_WRITE) {
										doHttpPost(list);
										list.clear();
									}
								}
								if (list.size() > 0) {
									doHttpPost(list);
								}
							} catch (Exception e) {
								// ignore
							}
						}

						long elapsed = System.currentTimeMillis() - start;

						if (elapsed < m_interval) {
							try {
								Thread.sleep(m_interval - elapsed);
							} catch (InterruptedException e) {
								break;
							}
						}
					}
				} catch (Exception e) {
					// ingnore
				}
			}
		});
		m_thread.setName(getClass().getSimpleName());
		m_thread.setDaemon(true);
		m_running = true;
		m_thread.start();
	}

	private static class Item {
		private String m_key;

		private String m_value;

		private long m_mills;

		private Item(String key, String value, long mills) {
			m_key = key;
			m_value = value;
			m_mills = mills;
		}
	}
}
