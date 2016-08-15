package org.unidal.cat.spi.analysis.pipeline;

import org.unidal.cat.spi.analysis.MessageRoutingStrategy;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.message.spi.MessageTree;

@Named(type = MessageRoutingStrategy.class, value = StrategyConstants.DOMAIN_HASH, instantiationStrategy = Named.PER_LOOKUP)
public class DomainHashStrategy implements MessageRoutingStrategy {
	@Override
	public int getIndex(MessageTree tree, int size) {
		return Math.abs(tree.getDomain().hashCode()) % size;
	}
}
