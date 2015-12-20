package org.unidal.cat.report.spi;

import java.util.Collection;

import org.unidal.cat.report.Report;
import org.unidal.cat.report.ReportPeriod;

public interface ReportAggregator<T extends Report> {
	public T aggregate(ReportPeriod period, Collection<T> reports);
}
