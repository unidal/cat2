package org.unidal.cat.message.storage;

public interface BlockDumperManager {

	public abstract void closeDumper(int hour);

	public abstract BlockDumper findDumper(int hour);

	public abstract BlockDumper findOrCreateBlockDumper(int hour);

}