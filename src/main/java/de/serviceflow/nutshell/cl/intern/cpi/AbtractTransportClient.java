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
package de.serviceflow.nutshell.cl.intern.cpi;

import java.net.InetSocketAddress;
import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.APState;
import de.serviceflow.nutshell.cl.ApplicationProtocol;
import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.SessionState;
import de.serviceflow.nutshell.cl.intern.Communication;
import de.serviceflow.nutshell.cl.intern.NIOTransportProvider;
import de.serviceflow.nutshell.cl.intern.NioSession;
import de.serviceflow.nutshell.cl.intern.SessionObject;
import de.serviceflow.nutshell.cl.intern.namp.ClientAuthentication;
import de.serviceflow.nutshell.cl.intern.namp.MessageClassification;

/**
 * Base class for transport clients.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 88541d5af5d9b949e366559da548f15e621166ad $
 * 
 * 
 */
public abstract class AbtractTransportClient extends NIOTransportProvider
		implements TransportClient {
	private static final Logger jlog = Logger
			.getLogger(AbtractTransportClient.class.getName());

	private byte[] credentials;

	private final String applicationProtocolName;
	
	protected AbtractTransportClient(String applicationProtocolName) {
		if (ApplicationProtocol.getByName(applicationProtocolName)==null)
			throw new Error("Protocol not registered: "+applicationProtocolName);
		this.applicationProtocolName = applicationProtocolName;
		
	}
	

	public void init(Communication c, InetSocketAddress a,
			byte[] credentials) {
		super.init(c, a);

		this.credentials = credentials;
	}

	protected void authenticate(NioSession session, String applicationProtocolName) {
		ClientAuthentication m = (ClientAuthentication) Message.requestMessage(
				ClientAuthentication.class,
				MessageClassification.SESSION.value());
		m.requestedNampVersion = 1;
		m.requestedProtocol.setString(applicationProtocolName);
		m.credentials.addBytes(credentials);
		m.dualChannel = session.isDualChannel();
		m.sessionkey = session.getSessionkey();
		session.getOutgoingMessages(this).add(m);
	}

	public void sessionAccepted(NioSession session) {
	}

	public byte[] getCredentials() {
		return credentials;
	}

	public void changedApplicationProtocolState(SessionObject session,
			APState protocolState) {
		jlog.info("changedApplicationProtocolState " + session + " to "
				+ protocolState.getName());
		session.setSessionState(SessionState.SYNC);
	}

	public String getApplicationProtocolName() {
		return applicationProtocolName;
	}

}
