package org.unidal.cat.core.report;

import org.unidal.cat.core.config.CatConfigModule;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;
import org.unidal.lookup.annotation.Named;

@Named(type = Module.class, value = CatReportModule.ID)
public class CatReportModule extends AbstractModule {
	public static final String ID = "cat-core-report";

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules(CatConfigModule.ID);
	}

	@Override
	protected void execute(ModuleContext ctx) throws Exception {
	}
}
