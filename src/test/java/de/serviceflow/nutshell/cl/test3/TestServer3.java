package de.serviceflow.nutshell.cl.test3;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.MessageListener;
import de.serviceflow.nutshell.cl.Session;
import de.serviceflow.nutshell.cl.SimpleServerDaemon;

public class TestServer3 extends SimpleServerDaemon {
	public void bind() throws UnknownHostException {
		addApplicationProtocol(createProtocolReader());
		bind(new InetSocketAddress(InetAddress.getLocalHost(), 10000), this,
				new MyCom());
	}

	class MyCom implements MessageListener {
		@Override
		public void messageReceived(Session s, Message<?> m) {
		}
	}

	private Reader createProtocolReader() {
		return new InputStreamReader(getClass().getResourceAsStream(
				"/de/serviceflow/nutshell/cl/test/myprotocol.xml"));
	}
}
