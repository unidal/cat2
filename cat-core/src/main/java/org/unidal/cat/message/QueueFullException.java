package org.unidal.cat.message;

public class QueueFullException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public QueueFullException(String message) {
		super(message);
	}
}
