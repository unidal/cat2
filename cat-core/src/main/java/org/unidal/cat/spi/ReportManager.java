package org.unidal.cat.spi;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ReportManager<T extends Report> {
    public void doCheckpoint(int hour, int index, boolean atEnd) throws IOException;

    public void doInitLoad(int hour, int index) throws IOException;

    public T getLocalReport(String domain, int hour, int index, boolean createIfNotExist);

    // public Set<String> getDomains(ReportPeriod period, Date startTime);

    public List<T> getLocalReports(ReportPeriod period, Date startTime, String domain) throws IOException;

    public List<T> getLocalFileReport(ReportPeriod period, Date startTime, String domain) throws IOException;

    public List<Map<String, T>> getLocalReports(ReportPeriod period, int hour) throws IOException;

    public T getReport(ReportPeriod period, Date startTime, String domain, String filterId, String... keyValuePairs)
            throws IOException;

    public void removeReport(int hour, int index);
}
