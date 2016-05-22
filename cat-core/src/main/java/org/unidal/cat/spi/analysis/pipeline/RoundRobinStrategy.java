package org.unidal.cat.spi.analysis.pipeline;

import org.unidal.cat.spi.analysis.MessageRoutingStrategy;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageRoutingStrategy.class, value = StrategyConstants.ROUND_ROBIN, instantiationStrategy = Named.PER_LOOKUP)
public class RoundRobinStrategy implements MessageRoutingStrategy {
	private int index = 0;

	@Override
	public int getIndex(MessageTree tree, int size) {
		return (index++) % size;
	}
}
