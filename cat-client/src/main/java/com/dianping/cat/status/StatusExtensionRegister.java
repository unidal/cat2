package com.dianping.cat.status;

import java.util.ArrayList;
import java.util.List;

public class StatusExtensionRegister {

	private List<StatusExtension> m_extensions = new ArrayList<StatusExtension>();

	public static final StatusExtensionRegister s_register = new StatusExtensionRegister();

	public static StatusExtensionRegister getInstance() {
		return s_register;
	}

	private StatusExtensionRegister() {
	}

	public List<StatusExtension> getStatusExtension() {
		synchronized (this) {
			return m_extensions;
		}
	}

	public void register(StatusExtension extension) {
		synchronized (this) {
			m_extensions.add(extension);
		}
	}

	public void unregister(StatusExtension extension) {
		synchronized (this) {
			m_extensions.remove(extension);
		}
	}
}
