package org.unidal.cat.core.report.menu;

import org.unidal.web.mvc.ActionContext;

public interface MenuLinkBuilder {
	public String build(ActionContext<?> ctx);
}
