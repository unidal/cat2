package org.unidal.cat.spi.report;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportPeriod;

public interface ReportManager<T extends Report> {
   /**
    * Gets local report from memory for specific doman.
    * 
    * @param domain
    *           domain
    * @param hour
    *           the hours since January 1, 1970, 00:00:00 GMT.
    * @param index
    *           index
    * @param createIfNotExist
    *           create a new report if it's not exist.
    * @return
    */
   public T getLocalReport(String domain, int hour, int index, boolean createIfNotExist);

   /**
    * persist in-memory lcoal reports to the storage, such as File System, or MySQL database.
    * 
    * @param hour
    *           the hours since January 1, 1970, 00:00:00 GMT.
    */
   public List<Map<String, T>> getLocalReports(int hour);

   /**
    * Gets the whole aggregated report for given parameters and filter.
    * 
    * @param period
    * @param startTime
    * @param domain
    * @param filterId
    * @param keyValuePairs
    * @return
    * @throws IOException
    */
   public T getReport(ReportPeriod period, Date startTime, String domain, String filterId, String... keyValuePairs)
         throws IOException;

   /**
    * Gets all local reports for the given parameters.
    * 
    * @param period
    * @param startTime
    * @param domain
    * @param properties
    * @return
    * @throws IOException
    */
   public List<T> getReports(ReportPeriod period, Date startTime, String domain, Map<String, String> properties)
         throws IOException;

   /**
    * Loads local reports from File System if have.
    * 
    * @param hour
    *           the hours since January 1, 1970, 00:00:00 GMT.
    * @param index
    * @throws IOException
    */
   public void loadLocalReports(int hour, int index) throws IOException;

   /**
    * Removes local reports out of memory.
    * 
    * @param hour
    *           the hours since January 1, 1970, 00:00:00 GMT.
    */
   public void removeLocalReports(int hour);
}
