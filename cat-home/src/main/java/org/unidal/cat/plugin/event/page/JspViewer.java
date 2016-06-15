package org.unidal.cat.plugin.event.page;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case HOURLY_REPORT:
			return JspFile.HOURLY_REPORT.getPath();
        case HOURLY_GRAPH:
            return JspFile.HOURLY_GRAPH.getPath();
        case HISTORY_REPORT:
			return JspFile.HISTORY_REPORT.getPath();
		case HISTORY_GRAPH:
			return JspFile.HISTORY_GRAPH.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
