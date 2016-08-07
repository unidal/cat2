package org.unidal.cat.core.report.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.cat.core.report.CatReportModule;
import org.unidal.cat.core.report.menu.DefaultMenuManager;
import org.unidal.cat.core.report.nav.DomainGroupBar;
import org.unidal.cat.core.report.view.svg.DefaultGraphBuilder;
import org.unidal.cat.core.report.view.svg.DefaultValueTranslater;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(CatReportModule.class));

		all.add(A(DefaultMenuManager.class));
		all.add(A(DomainGroupBar.class));

		all.add(A(DefaultGraphBuilder.class));
		all.add(A(DefaultValueTranslater.class));

		return all;
	}
}
