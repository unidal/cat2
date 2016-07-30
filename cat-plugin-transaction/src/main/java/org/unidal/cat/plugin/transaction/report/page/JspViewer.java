package org.unidal.cat.plugin.transaction.report.page;

import org.unidal.cat.plugin.transaction.report.ReportPage;
import org.unidal.cat.spi.ReportPeriod;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case REPORT:
			return JspFile.REPORT.getPath();
		case GRAPH:
			if (ctx.getPayload().getReportPeriod() == ReportPeriod.HOUR) {
				return JspFile.HOURLY_GRAPH.getPath();
			} else {
				return JspFile.HISTORY_GRAPH.getPath();
			}
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
