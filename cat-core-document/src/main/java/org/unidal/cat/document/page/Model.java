package org.unidal.cat.document.page;

import java.util.List;

import org.unidal.cat.document.DocumentPage;
import org.unidal.cat.document.spi.Document;
import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<DocumentPage, Action, Context> {
	private List<Document> m_documents;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public List<Document> getDocuments() {
		return m_documents;
	}

	public void setDocuments(List<Document> documents) {
		m_documents = documents;
	}
}
