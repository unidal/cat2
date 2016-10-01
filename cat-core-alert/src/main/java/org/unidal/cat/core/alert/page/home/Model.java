package org.unidal.cat.core.alert.page.home;

import org.unidal.cat.core.alert.page.AlertPage;
import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<AlertPage, Action, Context> {
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
}
