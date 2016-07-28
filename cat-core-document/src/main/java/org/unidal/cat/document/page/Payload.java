package org.unidal.cat.document.page;

import org.unidal.cat.document.DocumentPage;
import org.unidal.cat.document.spi.Document;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<DocumentPage, Action> {
	private DocumentPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("id")
	private Document m_document;

	@Override
	public Action getAction() {
		return m_action;
	}

	public Document getDocument() {
		return m_document;
	}

	@Override
	public DocumentPage getPage() {
		return m_page;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setDocument(String id) {
		m_document = Document.getById(id, Document.INDEX);
	}

	@Override
	public void setPage(String page) {
		m_page = DocumentPage.getByName(page, DocumentPage.HOME);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}

		if (m_document == null) {
			m_document = Document.INDEX;
		}
	}
}
