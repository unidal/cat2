package org.unidal.cat.message.storage;

public interface MessageDumperManager {
	public abstract void closeDumper(int hour);

	public abstract MessageDumper findDumper(int hour);

	public abstract MessageDumper findOrCreateMessageDumper(int hour);
}