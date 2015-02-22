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
package de.serviceflow.nutshell.cl.intern.session;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.intern.Communication;
import de.serviceflow.nutshell.cl.intern.NioSession;
import de.serviceflow.nutshell.cl.intern.SessionObject;
import de.serviceflow.nutshell.cl.intern.cpi.TCPClient;
import de.serviceflow.nutshell.cl.intern.cpi.UDPClient;

/**
 * Dual channel client (TCP + UDP).
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 64bda492741229e9e1dc5cf82eccacca4995caba $
 * 
 * 
 */
public class NampTCPClient extends TCPClient {
	private static final Logger JLOG = Logger.getLogger(NampTCPClient.class
			.getName());

	private final UDPClient udp;
	private InetSocketAddress udpIsa;
	
	public NampTCPClient(String applicationProtocolName) {
		super(applicationProtocolName); 
		udp = new NampUDPClient(applicationProtocolName);
	}

	@Override
	public final void init(Communication c, InetSocketAddress isa,
			byte[] credentials) {
		super.init(c, isa, credentials);

		udpIsa = new InetSocketAddress(isa.getAddress(), isa.getPort() + 1);
		udp.init(c, udpIsa, credentials);
	}

	public final void start() throws IOException {
		super.start();
		// udp is started in authenticate(...)
	}

	public final void stop() throws IOException {
		super.stop();
		udp.stop();
	}

	public final void completeStop() throws IOException {
		super.completeStop();
		udp.completeStop();
	}

	public final boolean process() throws IOException {
		boolean hasNothingSelected1 = super.process();
		boolean hasNothingSelected2 = udp.process();
		return hasNothingSelected1 & hasNothingSelected2;
	}

	@Override
	protected final void authenticate(NioSession session, String applicationProtocolName) {
		// create dual channel.
		session.join(udp);

		// Authenticate TCP
		super.authenticate(session, applicationProtocolName);

		// ((NampUDPClient) udp).setSession(session);
	}

	/**
	 * After accepting TCP ...
	 */
	public final void sessionAccepted(NioSession session) {
		if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
			JLOG.log(SessionObject.MSG_TRACE_LEVEL,
					"****************** Starting 2nd Channel Authentication for key #"
							+ session.getSessionkey() + " on " + session);
		}

		try {
			// just start UDP
			udp.start();

			// this will call NampUDPClient->authenticate(NioSession)
			udp.addSessionAndAuthenticate(session);

		} catch (IOException e) {
			JLOG.log(Level.SEVERE, "NAMP connection failed.", e);
		}
	}

	private class NampUDPClient extends UDPClient {
		// private NioSession session;

		public NampUDPClient(String applicationProtocolName) {
			super(applicationProtocolName); 
		}

		
		@Override
		protected void authenticate(NioSession session, String applicationProtocolName) {
			ClientAuthentication m = (ClientAuthentication) Message
					.requestMessage(ClientAuthentication.class,
							MessageClassification.SESSION.value());
			m.requestedNampVersion = 1;
			m.requestedProtocol.setString(applicationProtocolName);
			m.sessionkey = 0L;
			m.credentials.addBytes(getCredentials());
			m.dualChannel = session.isDualChannel();

			// 2nd Authentication will be send over UDP!
			session.send(m);
		}

		@Override
		protected void authenticate(InetSocketAddress isa) {
			// create no new session to authenticate.
		}

		// public void setSession(NioSession session) {
		// // this.session = session;
		// }

	}
}
