package org.unidal.cat.spi.report;

import java.util.Collection;

import org.unidal.cat.spi.Report;
import org.unidal.cat.spi.ReportPeriod;

public interface ReportAggregator<T extends Report> {
	public T aggregate(ReportPeriod period, Collection<T> reports);

	public T makeAllReport(ReportPeriod period, Collection<T> reports);
}
