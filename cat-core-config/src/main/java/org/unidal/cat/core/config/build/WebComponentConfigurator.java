package org.unidal.cat.core.config.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.config.page.ConfigModule;

import org.unidal.lookup.configuration.Component;
import org.unidal.web.configuration.AbstractWebComponentsConfigurator;

class WebComponentConfigurator extends AbstractWebComponentsConfigurator {
	@SuppressWarnings("unchecked")
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		defineModuleRegistry(all, ConfigModule.class, ConfigModule.class);

		return all;
	}
}
