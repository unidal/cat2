package org.unidal.cat.message;

import org.unidal.cat.message.command.Command;
import org.unidal.net.transport.TransportHub;

import com.dianping.cat.message.spi.MessageTree;

public interface ClientTransportHub extends TransportHub {
	public void sendCommand(Command cmd);

	public void sendHeartbeat(MessageTree heartbeat);

	public void sendMessageTree(MessageTree tree);
}
