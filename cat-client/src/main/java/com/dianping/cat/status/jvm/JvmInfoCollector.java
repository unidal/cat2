package com.dianping.cat.status.jvm;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.dianping.cat.Cat;
import com.dianping.cat.status.AbstractCollector;
import com.dianping.cat.status.StatusExtensionRegister;

public class JvmInfoCollector {

	private static JvmInfoCollector s_collector = new JvmInfoCollector();

	private boolean m_hasOldGc = false;

	private long m_lastGcCount = 0;

	private long m_lastGcTime = 0;

	private long m_lastFullgcTime = 0;

	private long m_lastFullgcCount = 0;

	private long m_lastYounggcTime = 0;

	private long m_lastYounggcCount = 0;
	
	private Set<String> younggcAlgorithm = new LinkedHashSet<String>() {
		private static final long serialVersionUID = -2953196532584721351L;

		{
			add("Copy");
			add("ParNew");
			add("PS Scavenge");
			add("G1 Young Generation");
		}
	};
	
	private Set<String> oldgcAlgorithm = new LinkedHashSet<String>() {
		private static final long serialVersionUID = -8267829533109860610L;

		{
			add("MarkSweepCompact");
			add("PS MarkSweep");
			add("ConcurrentMarkSweep");
			add("G1 Old Generation");
		}
	};

	public static Map<String, String> convert(Map<String, Number> map) {
		Map<String, String> result = new LinkedHashMap<String, String>();

		for (Entry<String, Number> entry : map.entrySet()) {
			result.put(entry.getKey(), entry.getValue().toString());
		}
		return result;
	}

	public static JvmInfoCollector getInstance() {
		return s_collector;
	}

	private JvmInfoCollector() {
	}

	private Map<String, Number> doGcCollect() {
		long gcCount = 0;
		long gcTime = 0;
		long oldGCount = 0;
		long oldGcTime = 0;
		long youngGcCount = 0;
		long youngGcTime = 0;
		Map<String, Number> map = new LinkedHashMap<String, Number>();

		for (final GarbageCollectorMXBean garbageCollector : ManagementFactory.getGarbageCollectorMXBeans()) {
			gcTime += garbageCollector.getCollectionTime();
			gcCount += garbageCollector.getCollectionCount();
			String gcAlgorithm = garbageCollector.getName();

			if (younggcAlgorithm.contains(gcAlgorithm)) {
				youngGcTime += garbageCollector.getCollectionTime();
				youngGcCount += garbageCollector.getCollectionCount();
			} else if (oldgcAlgorithm.contains(gcAlgorithm)) {
				oldGcTime += garbageCollector.getCollectionTime();
				oldGCount += garbageCollector.getCollectionCount();
			} else {
				Cat.logEvent("UnknowGcAlgorithm", gcAlgorithm);
			}
		}

		map.put("jvm.gc.count", gcCount - m_lastGcCount);
		map.put("jvm.gc.time", gcTime - m_lastGcTime);
		final long value = oldGCount - m_lastFullgcCount;

		if (value > 0) {
			m_hasOldGc = true;
		}

		map.put("jvm.fullgc.count", value);
		map.put("jvm.fullgc.time", oldGcTime - m_lastFullgcTime);
		map.put("jvm.younggc.count", youngGcCount - m_lastYounggcCount);
		map.put("jvm.younggc.time", youngGcTime - m_lastYounggcTime);

		if (youngGcCount > m_lastYounggcCount) {
			map.put("jvm.younggc.meantime", (youngGcTime - m_lastYounggcTime) / (youngGcCount - m_lastYounggcCount));
		} else {
			map.put("jvm.younggc.meantime", 0);
		}

		m_lastGcCount = gcCount;
		m_lastGcTime = gcTime;
		m_lastYounggcCount = youngGcCount;
		m_lastYounggcTime = youngGcTime;
		m_lastFullgcCount = oldGCount;
		m_lastFullgcTime = oldGcTime;

		return map;
	}

	private Map<String, Number> doMemoryCollect() {
		MemoryInformations memInfo = new MemoryInformations();
		Map<String, Number> map = new LinkedHashMap<String, Number>();

		map.put("jvm.memory.used", memInfo.getUsedMemory());
		map.put("jvm.memory.used.percent", memInfo.getUsedMemoryPercentage());
		map.put("jvm.memory.nonheap.used", memInfo.getUsedNonHeapMemory());
		map.put("jvm.memory.nonheap.used.percent", memInfo.getUsedNonHeapPercentage());
		map.put("jvm.memory.oldgen.used", memInfo.getUsedOldGen());
		map.put("jvm.memory.oldgen.used.percent", memInfo.getUsedOldGenPercentage());

		if (m_hasOldGc) {
			map.put("jvm.memory.oldgen.used.percent.after.fullgc", memInfo.getUsedOldGenPercentage());
			m_hasOldGc = false;
		} else {
			map.put("jvm.memory.oldgen.used.percent.after.fullgc", 0);
		}

		map.put("jvm.memory.eden.used", memInfo.getUsedEdenSpace());
		map.put("jvm.memory.eden.used.percent", memInfo.getUsedEdenSpacePercentage());
		map.put("jvm.memory.survivor.used", memInfo.getUsedSurvivorSpace());
		map.put("jvm.memory.survivor.used.percent", memInfo.getUsedSurvivorSpacePercentage());
		map.put("jvm.memory.perm.used", memInfo.getUsedPermGen());
		map.put("jvm.memory.perm.used.percent", memInfo.getUsedPermGenPercentage());
		map.put("jvm.nio.directbuffer.used", memInfo.getUsedDirectBufferSize());
		map.put("jvm.nio.mapped.used", memInfo.getUsedMappedSize());

		return map;
	}

	public void registerJVMCollector() {
		final StatusExtensionRegister instance = StatusExtensionRegister.getInstance();

		instance.register(new AbstractCollector() {

			@Override
			public String getId() {
				return "jvm.gc";
			}

			@Override
			public Map<String, String> getProperties() {
				Map<String, Number> map = s_collector.doGcCollect();

				return convert(map);
			}
		});

		instance.register(new AbstractCollector() {


			@Override
			public String getId() {
				return "jvm.memory";
			}

			@Override
			public Map<String, String> getProperties() {
				Map<String, Number> map = s_collector.doMemoryCollect();

				return convert(map);
			}
		});
	}

}
