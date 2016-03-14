package org.unidal.cat.message.storage;

import java.io.File;
import java.util.Date;

public interface FileBuilder {
	public File getFile(String domain, Date startTime, String ip, FileType type);

	public static enum FileType {
		MAPPING("map"),

		INDEX("idx"),

		DATA("dat");

		private String m_extension;

		private FileType(String extension) {
			m_extension = extension;
		}

		public String getExtension() {
			return m_extension;
		}
	}
}
