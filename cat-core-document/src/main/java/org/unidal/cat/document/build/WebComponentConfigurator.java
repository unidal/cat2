package org.unidal.cat.document.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.document.DocumentModule;

import org.unidal.lookup.configuration.Component;
import org.unidal.web.configuration.AbstractWebComponentsConfigurator;

class WebComponentConfigurator extends AbstractWebComponentsConfigurator {
	@SuppressWarnings("unchecked")
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		defineModuleRegistry(all, DocumentModule.class, DocumentModule.class);

		return all;
	}
}
