package org.unidal.cat.spi.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.unidal.cat.spi.Report;

public class ReportAggregatorUtil {
	public static Map<String, Report> aggregateMapsOfReports(ReportAggregator<Report> aggregator,
	      List<Map<String, ? extends Report>> reportMaps) {
		Map<String, Report> aggregatedReports = new ConcurrentHashMap<String, Report>();
		Map<String, List<Report>> originalReports = new ConcurrentHashMap<String, List<Report>>();
		for (Map<String, ? extends Report> reports : reportMaps) {
			if (null == reports)
				continue;
			for (Map.Entry<String, ? extends Report> entry : reports.entrySet()) {
				String domain = entry.getKey();
				Report value = entry.getValue();
				List<Report> originalReportList = originalReports.get(domain);
				if (null == originalReportList) {
					originalReportList = new ArrayList<Report>(reportMaps.size());
					originalReports.put(domain, originalReportList);
				}
				originalReportList.add(value);
			}
		}

		for (Map.Entry<String, List<Report>> entry : originalReports.entrySet()) {
			String domain = entry.getKey();
			List<Report> originalReportList = entry.getValue();
			Report aggregated = aggregator.aggregate(null, originalReportList);
			aggregatedReports.put(domain, aggregated);
		}
		return aggregatedReports;
	}

}
