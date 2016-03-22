package org.unidal.cat.spi;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface ReportManager<T extends Report> {
	public void doCheckpoint(Date date, int index, boolean atEnd) throws IOException;

	public void doInitLoad(Date date, int index) throws IOException;

	public T getLocalReport(String domain, Date startTime, int index, boolean createIfNotExist);

	// public Set<String> getDomains(ReportPeriod period, Date startTime);

	public List<T> getLocalReports(ReportPeriod period, Date startTime, String domain) throws IOException;

	public T getReport(ReportPeriod period, Date startTime, String domain, String filterId, String... keyValuePairs)
	      throws IOException;
}
