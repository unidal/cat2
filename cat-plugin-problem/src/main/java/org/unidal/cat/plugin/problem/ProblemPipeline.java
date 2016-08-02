package org.unidal.cat.plugin.problem;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.core.document.spi.Document;
import org.unidal.cat.core.report.menu.MenuLinkBuilder;
import org.unidal.cat.core.report.menu.MenuManager;
import org.unidal.cat.spi.analysis.pipeline.AbstractPipeline;
import org.unidal.cat.spi.analysis.pipeline.Pipeline;
import org.unidal.lookup.annotation.Named;
import org.unidal.web.mvc.ActionContext;

@Named(type = Pipeline.class, value = ProblemConstants.NAME, instantiationStrategy = Named.PER_LOOKUP)
public class ProblemPipeline extends AbstractPipeline implements Initializable {
	@Override
	protected void afterCheckpoint() {

	}

	@Override
	protected void beforeCheckpoint() {

	}

	@Override
	public void initialize() throws InitializationException {
		lookup(MenuManager.class).register("problem", "Problem", "fa fa-bug", new MenuLinkBuilder() {
			@Override
			public String build(ActionContext<?> ctx) {
				return ctx.getQuery().uri("/r/p").toString();
			}
		});

		Document.USER.register("problem", "Problem");
	}
}
