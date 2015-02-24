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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.xml.bind.JAXBException;

import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.MessageListener;
import de.serviceflow.nutshell.cl.Session;
import de.serviceflow.nutshell.cl.SimpleServerDaemon;
import de.serviceflow.nutshell.cl.intern.TransportProvider;

/**
 * Example for an application's stand-alone Server daemon.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: $
 * 
 * 
 */
public class TestServerDaemon1 extends SimpleServerDaemon implements
		MessageListener {
	static final Logger JLOG = Logger.getLogger(TestServerDaemon1.class
			.getName());
	/**
	 * 
	 */
	private final SimpleCommunicationTest simpleCommunicationTest;

	public TestServerDaemon1(SimpleCommunicationTest simpleCommunicationTest,
			MBeanServer server) {
		this.simpleCommunicationTest = simpleCommunicationTest;

		setMbeanServer(server);

		addApplicationProtocol(this.simpleCommunicationTest
				.createProtocolReader());
	}

	public final void startDaemon() throws UnknownHostException,
			ClassNotFoundException, IllegalAccessException, JAXBException {
		bind(new InetSocketAddress(InetAddress.getLocalHost(),
				simpleCommunicationTest.TESTPORT), this, this);

	}

	public final void messageHasBeenSent(Session s, Message m) {
		JLOG.fine("TestServerDaemon2 detects messageSend " + m);
	}

	public final void messageReceived(Session s, Message nextMessage) {
		JLOG.fine("TestServerDaemon2 detects messageReceived " + nextMessage);
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
			if (nextMessage instanceof TestRequest) {
				TestRequest request = (TestRequest) nextMessage;
				TestResponse response = (TestResponse) Message.requestMessage(
						TestResponse.class, s);
				// calculate clientCalculatedProduct and send it back to
				// client
				response.result = request.factor1 * request.factor2;
				JLOG.info("Calculated " + request.factor1 + " * "
						+ request.factor2 + " = " + response.result);
				Object o = request.expected.getObjective();
				if (!(o instanceof Expected)) {
					fail("o not instanceof Expected");
				}
				Expected x = (Expected) o;
				if (x.getResult() == response.result) {
					JLOG.info("Expected result identical");
				} else {
					fail("Expected result different: " + x.getResult());
				}
				s.send(response);

				// Terminate Protocol
				// s.setApplicationProtocolState(getApplicationProtocol()
				// .getState("Terminated"));
			} else {
				fail("APPLICATION type unexpected: "
						+ nextMessage.getProtocolId() + "/"
						+ nextMessage.getCommandId());
			}
		} finally {
			nextMessage.releaseMessage();
		}
	}

	public final void sessionCreated(TransportProvider p, Session mc) {
		addCommunicationSession(mc, new Object());
		JLOG.info("TestServerDaemon2#" + TestServerDaemon1.this.hashCode()
				+ " detects connectionEstablished");
	}

	@Override
	public void connectionLost(TransportProvider p, Session mc) {
		JLOG.info("TestServerDaemon2#" + TestServerDaemon1.this.hashCode()
				+ " detects connectionLost");
	}

}