package org.unidal.cat.plugin.transaction.report;

import org.unidal.cat.spi.ReportPeriod;
import org.unidal.web.mvc.Action;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.Page;

public class ReportContext<T extends ActionPayload<? extends Page, ? extends Action>> extends ActionContext<T> {
	@Override
	public Query getQuery() {
		return new Query(getHttpServletRequest(), true);
	}

	public ReportPeriod getPeriod() {
		String period = this.getHttpServletRequest().getParameter("period");

		return ReportPeriod.getByName(period, ReportPeriod.HOUR);
	}
}
