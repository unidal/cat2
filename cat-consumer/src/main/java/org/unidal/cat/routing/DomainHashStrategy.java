package org.unidal.cat.routing;

import com.dianping.cat.Constants;
import com.dianping.cat.message.spi.MessageTree;
import org.unidal.cat.spi.analysis.MessageRoutingStrategy;
import org.unidal.lookup.annotation.Named;

@Named(type = MessageRoutingStrategy.class, value = Constants.DEFAULT, instantiationStrategy = Named.PER_LOOKUP)
public class DomainHashStrategy implements MessageRoutingStrategy {
    @Override
    public int getIndex(MessageTree tree, int size) {
        return Math.abs(tree.getDomain().hashCode()) % size;
    }
}
