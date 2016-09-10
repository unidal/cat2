package org.unidal.cat.core.config.page.service;

import org.unidal.cat.core.config.page.ConfigPage;
import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<ConfigPage, Action, Context> {
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
}
