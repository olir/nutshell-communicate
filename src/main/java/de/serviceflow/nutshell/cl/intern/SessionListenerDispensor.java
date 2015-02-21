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
package de.serviceflow.nutshell.cl.intern;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.MessageListener;
import de.serviceflow.nutshell.cl.Session;
import de.serviceflow.nutshell.cl.SessionListener;

/**
 * Utility class for transport to report communication events.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: $
 * 
 * 
 */
public class SessionListenerDispensor implements MessageListener,
		SessionListener, ConnectionListener {
	static private final Logger JLOG = Logger
			.getLogger(SessionListenerDispensor.class.getName());
	/**
	 * 
	 */
	private final Communication abstractCommunication;

	/**
	 * @param abstractCommunication
	 */
	public SessionListenerDispensor(Communication abstractCommunication) {
		this.abstractCommunication = abstractCommunication;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void sessionCreated(TransportProvider p, Session mc) {
		if (JLOG.isLoggable(Level.FINE)) {
			JLOG.fine("connectionEstablished " + mc);
		}
		for (SessionListener listener : this.abstractCommunication
				.getSlisteners()) {
			listener.sessionCreated(p, mc);
		}
		if (JLOG.isLoggable(Level.FINE)) {
			if (mc.getUserObject() == null) {
				JLOG.fine("sessionCreated() did not set an user object for session "
						+ mc);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void sessionTerminated(TransportProvider p, Session mc) {
		if (JLOG.isLoggable(Level.FINE)) {
			JLOG.fine("connectionTerminated " + mc);
		}
		for (SessionListener listener : this.abstractCommunication
				.getSlisteners()) {
			listener.sessionTerminated(p, mc);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void sessionFailedToOpen(TransportProvider p, Throwable t) {
		if (JLOG.isLoggable(Level.FINE)) {
			JLOG.log(Level.FINE, "connectionFailedToOpen " + t.getMessage(), t);
		}
		for (SessionListener listener : this.abstractCommunication
				.getSlisteners()) {
			listener.sessionFailedToOpen(p, t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void sessionRecovered(TransportProvider p, Session mc) {
		if (JLOG.isLoggable(Level.FINE)) {
			JLOG.fine("connectionOverload " + mc);
		}
		for (SessionListener listener : this.abstractCommunication
				.getSlisteners()) {
			listener.sessionRecovered(p, mc);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void sessionStall(TransportProvider p, Session mc) {
		if (JLOG.isLoggable(Level.FINE)) {
			JLOG.fine("connectionStall " + mc);
		}
		for (SessionListener listener : this.abstractCommunication
				.getSlisteners()) {
			listener.sessionStall(p, mc);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void stateChangeComplete(TransportProvider p, Session session) {
		if (JLOG.isLoggable(Level.FINE)) {
			JLOG.fine("stateChangeComplete " + session);
		}
		for (SessionListener listener : this.abstractCommunication
				.getSlisteners()) {
			listener.stateChangeComplete(p, session);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public final void connectionLost(TransportProvider p, NioSession mc) {
		if (JLOG.isLoggable(Level.FINE)) {
			JLOG.fine("connectionLost " + mc);
		}

		mc.stall();
	}

	/**
	 * {@inheritDoc}
	 */
	public final void connectionFixed(TransportProvider p, NioSession mc) {
		if (JLOG.isLoggable(Level.FINE)) {
			JLOG.fine("connectionFixed " + mc);
		}

	}

	public final void messageReceived(Session s, Message<?> nextMessage) {
		if (JLOG.isLoggable(Level.FINE)) {
			JLOG.fine("messageReceived " + nextMessage);
		}
		for (MessageListener listener : this.abstractCommunication
				.getMlisteners()) {
			listener.messageReceived(s, nextMessage);
		}
	}

	// /**
	// * {@inheritDoc}
	// */
	// public void messageArrived(SessionObject sc, AbstractMessage
	// m) {
	// if (JLOG.isLoggable(Level.FINE)) {
	// JLOG.fine("messageArrived " + m);
	// }
	// for (MessageChannelListener listener : mlisteners) {
	// listener.messageArrived(sc, m);
	// }
	// }
	//
}