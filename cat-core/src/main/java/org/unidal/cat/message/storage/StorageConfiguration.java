package org.unidal.cat.message.storage;

import java.io.File;

public interface StorageConfiguration {
	public boolean isLocalMode();

	public File getBaseDataDir();

	public void setBaseDataDir(File baseDataDir);
}