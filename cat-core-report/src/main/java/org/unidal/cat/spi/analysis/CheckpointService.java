package org.unidal.cat.spi.analysis;

public interface CheckpointService {
   public void doCheckpoint(String report, int hour) throws Exception;
}
