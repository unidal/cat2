package org.unidal.cat.core.document;

import org.unidal.web.mvc.Action;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.Page;

public class DocumentContext<T extends ActionPayload<? extends Page, ? extends Action>> extends ActionContext<T> {
	@Override
	public Query getQuery() {
		return new Query(getHttpServletRequest(), true);
	}
}
