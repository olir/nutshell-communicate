/*
 * Copyright 1999-2015 Oliver Rode http://www.serviceflow.de/nutshell
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.serviceflow.nutshell.cl.test2;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.MessageListener;
import de.serviceflow.nutshell.cl.NetworkProtocolType;
import de.serviceflow.nutshell.cl.Session;
import de.serviceflow.nutshell.cl.SimpleClient;
import de.serviceflow.nutshell.cl.intern.TransportProvider;

/**
 * Example for an application's client.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: $
 * 
 * 
 */
public class TestClient2 extends SimpleClient implements MessageListener {
	static final Logger jlog = Logger.getLogger(TestClient2.class.getName());

	public final int TESTPORT = 10101;

	private TestLoop loop;

	private final int amount;
	private final long sleepTime;
	private final int msgPerSleep;

	/**
	 * Setup protocol and listeners.
	 * 
	 * @param simpleCommunicationTest
	 *            TODO
	 */
	public TestClient2(int amount, long sleepTime, int msgPerSleep) {
		this.amount = amount;
		this.sleepTime = sleepTime;
		this.msgPerSleep = msgPerSleep;

		addApplicationProtocol(createProtocolReader());
	}

	public void connect(String host) throws UnknownHostException, IOException,
			ClassNotFoundException, IllegalAccessException, JAXBException {
		connect(NetworkProtocolType.TCP_UDP,
				new InetSocketAddress(InetAddress.getByName(host), TESTPORT),
				"test2/v1", "".getBytes(), this, this);
	}

	public void messageHasBeenSent(Session s, Message<?> m) {
		jlog.fine("TestClient2 detects messageSend " + m);
	}

	public void messageReceived(Session s, Message<?> nextMessage) {
		jlog.info("*** TestClient2 detects messageReceived " + nextMessage);
		try {
			/*
			 * Check if protocol instance for the current client is in the right
			 * state.
			 */
			if (s.inProtocolState("ReadyForTest")) {
				throw new Error("Protocol state invalid: "
						+ s.getApplicationProtocolState());
			}

			/*
			 * Get TEST_PING and send TEST_ACKNOWLEDGE.
			 */
			if (nextMessage.getCommand() == TestMessage2.TEST_ACKNOWLEDGE) {
				TestAcknowledge m = (TestAcknowledge) nextMessage;
				loop.stopTimer(m, s);
			} else {
				throw new Error("APPLICATION message unexpected: "
						+ nextMessage.getCommand());
			}
		} finally {
			nextMessage.releaseMessage();
		}
	}

	public void sessionCreated(TransportProvider p, Session session) {
		jlog.log(Level.INFO, "TestClient2#" + TestClient2.this.hashCode()
				+ " detects sessionCreated");
		// addCommunicationSession(mc, getTestData()); // remember session
		doTest(session, amount, sleepTime, msgPerSleep);
	}

	@Override
	public void sessionFailedToOpen(TransportProvider p, Throwable t) {
		jlog.log(Level.INFO, "connectionFailedToOpen: " + t);
	}

	@Override
	public void connectionLost(TransportProvider p, Session mc) {
		jlog.info("TestClient2#" + TestClient2.this.hashCode()
				+ " detects connectionLost");
	}

	public Reader createProtocolReader() {
		InputStream is = getClass().getResourceAsStream(
				"/de/serviceflow/nutshell/cl/test/testprotocol2.xml");
		if (is == null) {
			throw new Error("protocol resource not found");
		}
		return new InputStreamReader(is);
	}

	private void doTest(Session session, int amount, long sleepTime,
			int msgPerSleep) {
		loop = new TestLoop(session, amount, sleepTime, msgPerSleep);
		new Thread(loop).start();
	}

	public static void main(String[] args) {
		String host = "localhost";
		int amount = 1000;
		long sleepTime = 1L;
		int msgPerSleep = 1;

		try {
			if (args.length >= 1) {
				if (args[0].startsWith("-") || args[0].startsWith("?"))
					throw new Exception();
				host = args[0];
			}
			if (args.length >= 2) {
				amount = Integer.parseInt(args[1]);
			}
			if (args.length >= 3) {
				sleepTime = Long.parseLong(args[2]);
			}
			if (args.length >= 4) {
				msgPerSleep = Integer.parseInt(args[3]);
			}

		} catch (Exception e) {
			System.out
					.println("Usage: TestClient2 [hostname(localhost) [amount(1000) [sleepTime(1) [msgPerSleep(1)]]]] ");
			return;
		}

		// Setup a Server
		TestClient2 c = new TestClient2(amount, sleepTime, msgPerSleep);
		try {
			c.connect(host);
		} catch (ClassNotFoundException | IllegalAccessException
				| JAXBException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}