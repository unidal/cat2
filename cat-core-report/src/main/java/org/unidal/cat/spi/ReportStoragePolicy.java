package org.unidal.cat.spi;

public enum ReportStoragePolicy {
	FILE,

	MYSQL,

	FILE_AND_MYSQL;

	public boolean forMySQL() {
		return this == FILE_AND_MYSQL || this == MYSQL;
	}

	public boolean forFile() {
		return this == FILE_AND_MYSQL || this == FILE;
	}
}