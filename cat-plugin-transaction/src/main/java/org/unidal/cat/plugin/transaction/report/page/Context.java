package org.unidal.cat.plugin.transaction.report.page;

import org.unidal.cat.spi.ReportPeriod;
import org.unidal.web.mvc.ActionContext;

public class Context extends ActionContext<Payload> {
	@Override
	public Query getQuery() {
		return new Query(getHttpServletRequest(), true);
	}

	public ReportPeriod getPeriod() {
		String period = this.getHttpServletRequest().getParameter("period");

		return ReportPeriod.getByName(period, ReportPeriod.HOUR);
	}
}