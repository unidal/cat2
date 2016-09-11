package org.unidal.cat.core.message.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.message.page.MessageModule;

import org.unidal.lookup.configuration.Component;
import org.unidal.web.configuration.AbstractWebComponentsConfigurator;

class WebComponentConfigurator extends AbstractWebComponentsConfigurator {
	@SuppressWarnings("unchecked")
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		defineModuleRegistry(all, MessageModule.class, MessageModule.class);

		return all;
	}
}
