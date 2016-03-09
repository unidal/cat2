package org.unidal.cat.message.storage;

import java.io.File;

public interface StorageConfiguration {
	public File getBaseDataDir();

	public boolean isLocalMode();

	public void setBaseDataDir(File baseDataDir);
}