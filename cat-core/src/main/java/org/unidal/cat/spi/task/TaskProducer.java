package org.unidal.cat.spi.task;

import java.util.Date;

import org.unidal.cat.spi.ReportPeriod;

public interface TaskProducer {
	public void produce(ReportPeriod period, String type, String domain, Date startTime);
}
