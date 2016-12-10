package com.dianping.cat.status.system;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.status.AbstractCollector;

public class ProcessorInfoCollector extends AbstractCollector {

	private Map<String, Number> doProcessCollect() {
		Map<String, Number> map = new LinkedHashMap<String, Number>();
		final OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();

		if (isSunOsMBean(operatingSystem)) {
			final com.sun.management.OperatingSystemMXBean osBean = (com.sun.management.OperatingSystemMXBean) operatingSystem;

			map.put("system.load.average", osBean.getSystemLoadAverage());
			map.put("cpu.system.load.percent", osBean.getSystemCpuLoad() * 100);
			map.put("cpu.jvm.load.percent", osBean.getProcessCpuLoad() * 100);
			map.put("system.process.used.phyical.memory",
			      osBean.getTotalPhysicalMemorySize() - osBean.getFreePhysicalMemorySize());
			map.put("system.process.used.swap.size", osBean.getTotalSwapSpaceSize() - osBean.getFreeSwapSpaceSize());
		}
		
		return map;
	}

	private boolean isSunOsMBean(OperatingSystemMXBean operatingSystem) {
		final String className = operatingSystem.getClass().getName();

		return "com.sun.management.OperatingSystem".equals(className)
		      || "com.sun.management.UnixOperatingSystem".equals(className);
	}

	@Override
	public String getId() {
		return "system.process";
	}

	@Override
	public Map<String, String> getProperties() {
		Map<String, Number> map = doProcessCollect();

		return convert(map);
	}
}
