package com.dianping.cat.status.send;

public class SendLocalAgentException extends Exception {

	private static final long serialVersionUID = 1L;

	public SendLocalAgentException(String msg) {
		super(msg);
	}

	public SendLocalAgentException(String msg, Throwable e) {
		super(msg, e);
	}

}
