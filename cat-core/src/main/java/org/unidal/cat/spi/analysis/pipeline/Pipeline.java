package org.unidal.cat.spi.analysis.pipeline;

import com.dianping.cat.message.spi.MessageTree;

import java.io.IOException;

public interface Pipeline {
    public void initialize(int hour);

    public void destroy();

    public boolean analyze(MessageTree tree);

    public void checkpoint(boolean atEnd) throws IOException;

    public String getName();
}
