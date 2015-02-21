package de.serviceflow.nutshell.cl.test3;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.MessageListener;
import de.serviceflow.nutshell.cl.NetworkProtocolType;
import de.serviceflow.nutshell.cl.Session;
import de.serviceflow.nutshell.cl.SimpleClient;

public class TestClient3 extends SimpleClient{
	public void connect() throws UnknownHostException, IOException {
		addApplicationProtocol(createProtocolReader());
		connect(NetworkProtocolType.TCP_UDP,
				new InetSocketAddress(InetAddress.getLocalHost(), 10000),
				"test3/v1", "".getBytes(), this, new MyCom());
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
