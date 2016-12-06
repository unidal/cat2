package com.dianping.cat.status.system;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.status.AbstractCollector;

public class StaticInfoCollector extends AbstractCollector {

	private String m_jars;

	private void buildClasspath() {
		ClassLoader loader = StaticInfoCollector.class.getClassLoader();
		StringBuilder sb = new StringBuilder();

		buildClasspath(loader, sb);

		if (sb.length() > 0) {
			m_jars = sb.substring(0, sb.length() - 1);
		}
	}

	private void buildClasspath(ClassLoader loader, StringBuilder sb) {
		if (loader instanceof URLClassLoader) {
			URL[] urLs = ((URLClassLoader) loader).getURLs();
			for (URL url : urLs) {
				String jar = parseJar(url.toExternalForm());

				if (jar != null) {
					sb.append(jar).append(',');
				}
			}
			ClassLoader parent = loader.getParent();

			buildClasspath(parent, sb);
		}
	}

	@Override
	public String getId() {
		return "system.static";
	}

	@Override
	public Map<String, String> getProperties() {
		if (m_jars == null) {
			buildClasspath();
		}

		Map<String, String> map = new LinkedHashMap<String, String>();

		map.put("system.java.verision", System.getProperty("java.version"));
		map.put("system.java.classpath", m_jars);
		map.put("system.user.name", System.getProperty("user.name"));

		return map;
	}

	private String parseJar(String path) {
		if (path.endsWith(".jar")) {
			int index = path.lastIndexOf('/');

			if (index > -1) {
				return path.substring(index + 1);
			}
		}
		return null;
	}
}
