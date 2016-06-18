package org.unidal.cat.message.command;

import org.unidal.cat.message.command.Command;

public interface CommandDispatcher {
	public void dispatch(Command cmd);
}
