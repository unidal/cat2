package org.unidal.cat.routing;

import com.dianping.cat.message.spi.MessageTree;
import org.unidal.cat.spi.analysis.MessageRoutingStrategy;
import org.unidal.lookup.annotation.Named;

@Named(type = MessageRoutingStrategy.class, value = StrategyConstants.ROUNDROBIN, instantiationStrategy = Named.PER_LOOKUP)
public class RoundRobinStrategy implements MessageRoutingStrategy {

    private int index = 0;

    @Override
    public int getIndex(MessageTree tree, int size) {
        index = (index + 1) % size;
        return index;
    }
}
