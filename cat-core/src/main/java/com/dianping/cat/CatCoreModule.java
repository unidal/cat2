package com.dianping.cat;

import org.unidal.cat.spi.analysis.MessageDispatcher;
import org.unidal.cat.spi.analysis.event.TimeWindowManager;
import org.unidal.cat.spi.report.task.ReportTaskConsumer;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;
import org.unidal.lookup.annotation.Named;

@Named(type = Module.class, value = CatCoreModule.ID)
public class CatCoreModule extends AbstractModule {
	public static final String ID = "cat-core";

	@Override
	protected void execute(final ModuleContext ctx) throws Exception {
		ctx.lookup(MessageDispatcher.class);

		TimeWindowManager manager = ctx.lookup(TimeWindowManager.class);

		if (manager instanceof Task) {
			Threads.forGroup("Cat").start((Task) manager);
		}

		ReportTaskConsumer consumer = ctx.lookup(ReportTaskConsumer.class);

		Threads.forGroup("Cat").start(consumer);
	}

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules(CatClientModule.ID);
	}
}
