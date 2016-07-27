package org.unidal.cat.document.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.document.spi.DefaultDocumentManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(DefaultDocumentManager.class));

		all.addAll(new WebComponentConfigurator().defineComponents());
		
		return all;
	}
}
