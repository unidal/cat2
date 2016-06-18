package org.unidal.cat.message.command;

import org.unidal.cat.message.command.Command;
import org.unidal.lookup.annotation.Named;

@Named(type = CommandDispatcher.class)
public class DefaultCommandDispatcher implements CommandDispatcher {
	@Override
	public void dispatch(Command cmd) {
		// TODO Auto-generated method stub

	}
}
