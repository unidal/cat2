package org.unidal.cat.spi.message;

import org.unidal.cat.message.command.Command;
import org.unidal.net.transport.TransportHub;

public interface ServerTransportHub extends TransportHub {
	public void sendCommand(Command cmd);
}
