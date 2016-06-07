package org.unidal.cat.plugin.problem;

import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.message.spi.MessageTree;

public interface ProblemHandler {
    void handle(Machine machine, MessageTree tree);
}
