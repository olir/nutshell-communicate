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
package de.serviceflow.nutshell.cl;

import java.io.IOException;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.ConnectionEventListener;

import de.serviceflow.nutshell.cl.intern.NIOTransportProvider;

/**
 * Basic implementation for a client.
 * <p>
 * To create your client, start by creating a subclass for it.
 * <p>
 * Then define there a constructor and implement a calls to
 * {@link #setApplicationProtocol(ApplicationProtocol)} and
 * {@link #addConnectionEventListener(ConnectionEventListener)}.
 * <p>
 * Finally implement there calls to
 * {@link #start(NetworkProtocolType, InetSocketAddress, ApplicationProtocol)} ,
 * including the necessary subclasses, and a call to {@link #stop()}.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: c8d76ec3a82b4bc04fe3bbf01d0a17fd36643e1c $
 * 
 * 
 */
public abstract class SimpleClient extends DefaultSessionListener {
	static private Logger JLOG = Logger.getLogger(SimpleClient.class.getName());

	private ClientCommunication clientCommunication;
	private SessionListener slistener;
	private MessageListener mlistener;

	protected SimpleClient() {
	}

	protected final void addApplicationProtocol(Reader r) {
		if (clientCommunication==null) {
			clientCommunication = ClientCommunication.getClientCommunication();
		}
		clientCommunication.addApplicationProtocol(r);
	}

	
	/**
	 * Connect dual channel (TCP on port, and udp on port+1).
	 * 
	 * @param inetSocketAddress
	 * @param applicationProtocol
	 *            name and version (ASCII chars)
	 * @param credentials
	 * @param slistener
	 * @param mlistener
	 * @return
	 * @throws IOException
	 */
	public final NIOTransportProvider connect(NetworkProtocolType npt,
			InetSocketAddress inetSocketAddress, String protocolName,
			byte[] credentials, SessionListener slistener,
			MessageListener mlistener) throws IOException {
		if (clientCommunication==null) {
			throw new Error(
					"You need to add at least one application protocol first.");
		}
		try {
			this.slistener = slistener;
			this.mlistener = mlistener;
			clientCommunication.addSessionListener(slistener);
			clientCommunication.addMessageListener(mlistener);
			NIOTransportProvider c = clientCommunication.connect(npt,
					inetSocketAddress, protocolName, credentials);
			if (JLOG.isLoggable(Level.FINE)) {
				JLOG.fine("started.");
			}
			return c;
		} catch (IOException e) {
			JLOG.log(Level.WARNING, "protocol start failed", e);
			throw e;
		}

	}

	/**
	 */
	public void stop() {
		try {
			clientCommunication.stopAllClients();
			if (JLOG.isLoggable(Level.FINE)) {
				JLOG.fine("stopped.");
			}
			clientCommunication.removeAllListeners();
		} catch (Exception e) {
			JLOG.log(Level.WARNING, "protocol stop failed", e);
		} finally {
			clientCommunication.removeSessionListener(slistener);
			clientCommunication.removeMessageListener(mlistener);
		}
	}

}
