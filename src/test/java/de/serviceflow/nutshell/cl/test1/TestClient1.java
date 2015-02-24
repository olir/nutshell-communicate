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
package de.serviceflow.nutshell.cl.test1;

import static org.junit.Assert.fail;

import java.io.IOException;
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
public class TestClient1 extends SimpleClient implements MessageListener {
	static final Logger JLOG = Logger.getLogger(TestClient1.class.getName());
	/**
	 * 
	 */
	private final SimpleCommunicationTest simpleCommunicationTest;
	MuliplicationTest testData;

	/**
	 * Setup protocol and listeners.
	 * 
	 * @param simpleCommunicationTest
	 *            TODO
	 */
	public TestClient1(SimpleCommunicationTest simpleCommunicationTest) {
		this.simpleCommunicationTest = simpleCommunicationTest;
		setTestData(new MuliplicationTest(3, 7));

		addApplicationProtocol(simpleCommunicationTest.createProtocolReader());
	}

	/**
	 * Send a test message to server.
	 */
	public final void testMessaging() {
		Session s = getCommunicationSession(getTestData());
		TestRequest m = (TestRequest) Message.requestMessage(TestRequest.class,
				s);
		m.factor1 = getTestData().factor1;
		m.factor2 = getTestData().factor2;
		Expected x = new Expected(getTestData().factor1*getTestData().factor2);
		m.expected.setObjective(x);
		try {
			s.send(m);
		} catch (NullPointerException e) {
			fail("SESSION not created in time.");
		}
	}

	public void connect() throws UnknownHostException, IOException,
			ClassNotFoundException, IllegalAccessException, JAXBException {
		connect(NetworkProtocolType.TCP_UDP, new InetSocketAddress(InetAddress.getLocalHost(),
				simpleCommunicationTest.TESTPORT), "test1/v1",
				"anon:anon".getBytes(), this, this);
	}

	public final MuliplicationTest getTestData() {
		return testData;
	}

	public final void setTestData(MuliplicationTest testData) {
		this.testData = testData;
	}

	public final void messageHasBeenSent(Session s, Message m) {
		JLOG.fine("TestClient2 detects messageSend " + m);
	}

	public final void messageReceived(Session s, Message nextMessage) {
		JLOG.info("*** TestClient2 detects messageReceived " + nextMessage);
		try {
			/*
			 * Check if protocol instance for the current client is in the right
			 * state.
			 */
			if (!s.inProtocolState("ReadyForTest")) {
				fail("Protocol state invalid: "
						+ s.getApplicationProtocolState());
			}

			/*
			 * Get TEST_PING and send TEST_ACKNOWLEDGE.
			 */
			if (nextMessage instanceof TestResponse) {
				TestResponse response = (TestResponse) nextMessage;
				((MuliplicationTest) s.getUserObject())
						.setServerCalculatedProduct(response.result);

				// Terminate Protocol
				// s.setApplicationProtocolState(getApplicationProtocol().getState(
				// "Terminated"));
			} else {
				fail("APPLICATION type unexpected: " + nextMessage);
			}
		} finally {
			nextMessage.releaseMessage();
		}
	}

	public final void sessionCreated(TransportProvider p, Session mc) {
		JLOG.log(Level.INFO, "TestClient2#" + TestClient1.this.hashCode()
				+ " detects sessionCreated");
		addCommunicationSession(mc, getTestData()); // remember session
		testMessaging();
	}

	@Override
	public void sessionFailedToOpen(TransportProvider p, Throwable t) {
		fail("connectionFailedToOpen");
	}

	@Override
	public void connectionLost(TransportProvider p, Session mc) {
		JLOG.info("TestClient2#" + TestClient1.this.hashCode()
				+ " detects connectionLost");
	}

}