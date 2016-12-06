package com.dianping.cat.status.datasource;

import java.util.LinkedHashMap;
import java.util.Map;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;

public class DatabaseParser {

	private Map<String, Database> m_connections = new LinkedHashMap<String, Database>();

	public Database parseDatabase(String connection) {
		Database database = m_connections.get(String.valueOf(connection));

		if (database == null && StringUtils.isNotEmpty(connection)) {
			try {
				if (connection.contains("jdbc:mysql://")) {
					String con = connection.split("jdbc:mysql://")[1];
					con = con.split("\\?")[0];
					int index = con.indexOf(":");
					String ip = "";

					if (index < 0) {
						ip = con.split("/")[0];
					} else {
						ip = con.substring(0, index);
					}

					String name = con.substring(con.indexOf("/") + 1);
					database = new Database(name, ip);

					m_connections.put(connection, database);
				} else if (connection.contains("jdbc:oracle")) {
					if (connection.indexOf("DESCRIPTION") > -1) {
						String name = find(connection, "SERVICE_NAME");
						String ip = find(connection, "HOST");

						database = new Database(name, ip);
						m_connections.put(connection, database);
					} else if (connection.indexOf("@//") > -1) {
						String[] tabs = connection.split("/");
						String name = tabs[tabs.length - 1];
						String ip = tabs[tabs.length - 2];
						int index = ip.indexOf(':');

						if (index > -1) {
							ip = ip.substring(0, index);
						}
						database = new Database(name, ip);
						m_connections.put(connection, database);
					} else {
						String[] tabs = connection.split(":");
						String ip = "Default";

						for (String str : tabs) {
							int index = str.indexOf("@");

							if (index > -1) {
								ip = str.substring(index + 1).trim();
							}
						}
						String name = tabs[tabs.length - 1];
						int index = name.indexOf('/');

						if (index > -1) {
							name = name.substring(index + 1);
						}

						database = new Database(name, ip);

						m_connections.put(connection, database);
					}
				} else {
					return new Database("default", "default");
				}
			} catch (Exception e) {
				Cat.logError(connection, e);
			}
		}
		return database;
	}

	private String find(String con, String key) {
		int index = con.indexOf(key);
		int start = 0;
		int end = 0;
		if (index > -1) {
			for (int i = index + key.length(); i < con.length(); i++) {
				if (con.charAt(i) == '=') {
					start = i + 1;
				}
				if (con.charAt(i) == ')') {
					end = i;
					break;
				}
			}
		}
		return con.substring(start, end);
	}

	public static class Database {

		private String m_name;

		private String m_ip;

		public Database(String name, String ip) {
			m_name = name;
			m_ip = ip;
		}

		public String getIp() {
			return m_ip;
		}

		public String getName() {
			return m_name;
		}

		@Override
		public String toString() {
			return m_name + '_' + m_ip;
		}
	}
}
