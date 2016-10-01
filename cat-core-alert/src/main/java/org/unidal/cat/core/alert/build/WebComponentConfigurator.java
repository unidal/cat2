package org.unidal.cat.core.alert.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.alert.page.AlertModule;

import org.unidal.lookup.configuration.Component;
import org.unidal.web.configuration.AbstractWebComponentsConfigurator;

class WebComponentConfigurator extends AbstractWebComponentsConfigurator {
	@SuppressWarnings("unchecked")
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		defineModuleRegistry(all, AlertModule.class, AlertModule.class);

		return all;
	}
}
