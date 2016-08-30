package org.unidal.cat.core.config.system.page;

import org.unidal.cat.core.config.system.SystemPage;
import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<SystemPage, Action, Context> {
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
}
