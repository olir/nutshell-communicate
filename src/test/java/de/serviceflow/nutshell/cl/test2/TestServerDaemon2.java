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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.BitSet;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.xml.bind.JAXBException;

import de.serviceflow.nutshell.cl.Authentication;
import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.MessageListener;
import de.serviceflow.nutshell.cl.ServerCommunication;
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
public class TestServerDaemon2 extends SimpleServerDaemon implements
		MessageListener {
	static final Logger jlog = Logger.getLogger(TestServerDaemon2.class
			.getName());

	protected static final long OLD_WAITTIME = 1000L;

	public final int TESTPORT = 10101;

	int count = 0;
	int old = 0;
	int copies = 0;
	long muuid = 0L;
	TestAcknowledge response = null;
	static Session session = null;
	BitSet dChecker = null;

	public TestServerDaemon2(MBeanServer server) {
		super(server);

		addApplicationProtocol(createProtocolReader());
	}

	public void startDaemon() throws UnknownHostException,
			ClassNotFoundException, IllegalAccessException, JAXBException {

		bind(new InetSocketAddress(InetAddress.getLocalHost(), TESTPORT), this,
				this);

		getCommunication().addCommunicationWorker(new Runnable() {
			@Override
			public void run() {
				if (response != null) {
					if (response.old > 0) {
						try {
							jlog.info("waiting for more old messages...");
							response.oldwait += OLD_WAITTIME;
							Thread.sleep(OLD_WAITTIME);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if (old > 0) {
						jlog.info("removed " + old + " old messages.");
						old = 0;
					} else {
						session.send(response);
						response = null;
					}
				} else {
					try {
						Thread.sleep(1L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		jlog.info("TestServerDaemon2 is waiting for incoming connections on "
				+ InetAddress.getLocalHost() + ":" + TESTPORT);

	}

	public void messageHasBeenSent(Session s, Message<?> m) {
		jlog.fine("TestServerDaemon2 detects messageSend " + m);
	}

	public void messageReceived(Session s, Message<?> nextMessage) {
		jlog.fine("TestServerDaemon2 detects messageReceived " + nextMessage);
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
			if (nextMessage.getCommand() == TestMessage2.TEST_ROUND_STARTING) {
				TestRoundStarting m = (TestRoundStarting) nextMessage;
				muuid = m.muuid;
				if (dChecker == null) {
					dChecker = new BitSet(m.amount);
				} else {
					dChecker.clear();
				}
				jlog.info("start " + muuid);
			} else if (nextMessage.getCommand() == TestMessage2.TEST_PING) {
				TestPing m = (TestPing) nextMessage;
				if (muuid == m.muuid) {
					if (dChecker.get(m.id)) {
						copies++;
					} else {
						dChecker.set(m.id);
						count++;
					}
				} else
					old++;
			} else if (nextMessage.getCommand() == TestMessage2.TEST_ROUND_COMPLETED) {
				TestRoundCompleted m = (TestRoundCompleted) nextMessage;
				if (muuid != m.muuid) {
					throw new Error("UUID mismatch");
				}
				jlog.info("end.");

				TestAcknowledge r = (TestAcknowledge) Message.requestMessage(
						TestAcknowledge.class, s);
				r.count = count;
				r.old = old;
				r.copies = copies;
				r.oldwait = 0L;
				if (old > 0) {
					jlog.info("removed " + old + " old messages.");
				}
				count = 0;
				old = 0;
				response = r;
			} else {
				throw new Error("APPLICATION type unexpected: "
						+ nextMessage.getClassificationValue() + "/"
						+ nextMessage.getCommandId());
			}
		} finally {
			nextMessage.releaseMessage();
		}
	}

	public void sessionCreated(TransportProvider p, Session mc) {
		addCommunicationSession(mc, new Object());
		jlog.info("TestServerDaemon2#" + TestServerDaemon2.this.hashCode()
				+ " detects connectionEstablished");

		session = mc;
		count = 0;
		old = 0;
		copies = 0;
		muuid = 0L;
		response = null;
	}

	@Override
	public void connectionLost(TransportProvider p, Session mc) {
		jlog.info("TestServerDaemon2#" + TestServerDaemon2.this.hashCode()
				+ " detects connectionLost");
		session = null;
		count = 0;
		old = 0;
		copies = 0;
		muuid = 0L;
		response = null;
	}

	public Reader createProtocolReader() {
		InputStream is = getClass().getResourceAsStream(
				"/de/serviceflow/nutshell/cl/test/testprotocol2.xml");
		if (is == null) {
			throw new Error("protocol resource not found");
		}
		return new InputStreamReader(is);
	}

	static class SingleClientAuthentication implements Authentication {

		@Override
		public Principal authenticate(byte[] credentials) {
			if (session == null) {
				return new Principal() {

					@Override
					public String getName() {
						return "TestUser";
					}
				};
			} else {
				jlog.info("2nd client tried to connect. Rejected.");
				return null;
			}
		}

	}

	public static void main(String[] args) {
		// Setup a Server
		TestServerDaemon2 s = new TestServerDaemon2(null);
		ServerCommunication.getServerCommunication().setAuthentication(
				new SingleClientAuthentication());
		try {
			s.startDaemon();
		} catch (UnknownHostException | ClassNotFoundException
				| IllegalAccessException | JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}