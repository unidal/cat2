package org.unidal.cat.core.document.page;

import java.io.IOException;

import javax.servlet.ServletException;

import org.unidal.cat.core.document.DocumentPage;
import org.unidal.cat.core.document.spi.DocumentManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private DocumentManager m_manager;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "home")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "home")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);

		model.setAction(Action.VIEW);
		model.setPage(DocumentPage.HOME);

		model.setDocuments(m_manager.getDocuments());

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
}
