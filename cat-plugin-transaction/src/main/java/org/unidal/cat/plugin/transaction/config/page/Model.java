package org.unidal.cat.plugin.transaction.config.page;

import org.unidal.cat.plugin.transaction.config.ConfigPage;
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
