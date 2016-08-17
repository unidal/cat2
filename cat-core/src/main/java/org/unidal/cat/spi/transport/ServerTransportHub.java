package org.unidal.cat.spi.transport;

import org.unidal.cat.message.command.Command;
import org.unidal.net.transport.TransportHub;

public interface ServerTransportHub extends TransportHub {
	public void sendCommand(Command cmd);
}
