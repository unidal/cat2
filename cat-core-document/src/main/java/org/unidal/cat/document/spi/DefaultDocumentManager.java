package org.unidal.cat.document.spi;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Named;

@Named(type = DocumentManager.class)
public class DefaultDocumentManager implements DocumentManager, Initializable {
	private List<Document> m_documents;

	@Override
	public List<Document> getDocuments() {
		return m_documents;
	}

	@Override
	public void initialize() throws InitializationException {
		m_documents = new ArrayList<Document>();

		for (Document document : Document.values()) {
			m_documents.add(document);
		}
	}
}
