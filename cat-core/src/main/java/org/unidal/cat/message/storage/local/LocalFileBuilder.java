package org.unidal.cat.message.storage.local;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;

import org.unidal.cat.message.storage.FileBuilder;
import org.unidal.cat.message.storage.StorageConfiguration;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

@Named(type = FileBuilder.class, value = "local")
public class LocalFileBuilder implements FileBuilder {
	@Inject
	private StorageConfiguration m_config;

	@Override
	public File getFile(String domain, Date startTime, String ip, FileType type) {
		MessageFormat format;
		String path;

		switch (type) {
		case MAPPING:
			format = new MessageFormat("dump/{0,date,yyyy}-{0,date,MM}/{0,date,dd}/{0,date,HH}/{2}.{3}");
			path = format.format(new Object[] { startTime, null, ip, type.getExtension() });
			break;
		default:
			format = new MessageFormat("dump/{0,date,yyyy}-{0,date,MM}/{0,date,dd}/{0,date,HH}/{1}-{2}.{3}");
			path = format.format(new Object[] { startTime, domain, ip, type.getExtension() });
			break;
		}

		File baseDir = m_config.getBaseDataDir();

		return new File(baseDir, path);
	}
}
