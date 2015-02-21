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

import java.io.Reader;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.sql.ConnectionEventListener;

import de.serviceflow.nutshell.cl.intern.Communication;

/**
 * Basic implementation for a server.
 * <p>
 * To create your server, start by creating a subclass for it.
 * <p>
 * Implement a call to
 * {@link #start(InetSocketAddress[], NetworkProtocolType[], ApplicationProtocol, ConnectionEventListener)}
 * , including the necessary subclasses, to start the daemon.
 * <p>
 * Finally implement a calls to {@link #stop()}.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: ce5eacaa0acf681ce1629e3fff7dcede973eb441 $
 * 
 * 
 */
public abstract class SimpleServerDaemon extends DefaultSessionListener {
	private static final Logger JLOG = Logger
			.getLogger(SimpleServerDaemon.class.getName());

	private ServerCommunication sc;

	public SimpleServerDaemon() {
		ServerCommunication.getServerCommunication().setAuthentication(
				new AnonymousAuthentication());
	}

	public final void setMbeanServer(MBeanServer mbs) {
		Communication.setMbeanServer(mbs);
	}

	public final void addApplicationProtocol(Reader r) {
		if (sc == null) {
			sc = ServerCommunication.getServerCommunication();
		}
		sc.addApplicationProtocol(r);
	}

	protected void bind(InetSocketAddress isa, SessionListener slistener,
			MessageListener mlistener) {
		if (sc == null) {
			throw new Error(
					"You need to add at least one application protocol first.");
		}
		try {

			sc.addSessionListener(slistener);
			sc.addMessageListener(mlistener);

			sc.bind(isa);

			if (JLOG.isLoggable(Level.FINE)) {
				JLOG.fine("communication started.");
			}
		} catch (Exception e) {
			try {
				throw new Error("communication start failed.", e);
			} finally {
				stop();
			}
		}
	}

	/**
	 */
	public void stop() {
		try {
			sc.stopAllServices();
			if (JLOG.isLoggable(Level.FINE)) {
				JLOG.fine("communication stopped.");
			}
		} catch (Exception e) {
			JLOG.log(Level.SEVERE, "communication stop failed." + e.toString(),
					e);
		}
	}

	public ServerCommunication getCommunication() {
		return sc;
	}

	private static class AnonymousAuthentication implements Authentication {

		@Override
		public Principal authenticate(byte[] credentials) {
			return new Principal() {

				@Override
				public String getName() {
					return "Anonymous";
				}
			};
		}

	}

}
