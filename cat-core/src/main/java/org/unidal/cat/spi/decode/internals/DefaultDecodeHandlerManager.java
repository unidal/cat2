package org.unidal.cat.spi.decode.internals;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.spi.decode.DecodeHandler;
import org.unidal.cat.spi.decode.DecodeHandlerManager;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named(type = DecodeHandlerManager.class)
public class DefaultDecodeHandlerManager extends ContainerHolder implements DecodeHandlerManager, Initializable {
	private Map<String, DecodeHandler> m_cached = new HashMap<String, DecodeHandler>();

	@Override
	public DecodeHandler getHandler(ByteBuf buf) {
		byte[] data = new byte[3];

		buf.getBytes(0, data);

		String hint = new String(data);

		return m_cached.get(hint);
	}

	@Override
	public void initialize() throws InitializationException {
		Map<String, DecodeHandler> map = lookupMap(DecodeHandler.class);

		m_cached.putAll(map);
	}
}
