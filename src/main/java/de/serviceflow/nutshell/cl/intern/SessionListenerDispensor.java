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
	static private final Logger jlog = Logger
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
	public void sessionCreated(TransportProvider p, Session mc) {
		if (jlog.isLoggable(Level.FINE)) {
			jlog.fine("connectionEstablished " + mc);
		}
		for (SessionListener listener : this.abstractCommunication
				.getSlisteners()) {
			listener.sessionCreated(p, mc);
		}
		if (jlog.isLoggable(Level.FINE)) {
			if (mc.getUserObject() == null) {
				jlog.fine("sessionCreated() did not set an user object for session "
						+ mc);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void sessionTerminated(TransportProvider p, Session mc) {
		if (jlog.isLoggable(Level.FINE)) {
			jlog.fine("connectionTerminated " + mc);
		}
		for (SessionListener listener : this.abstractCommunication
				.getSlisteners()) {
			listener.sessionTerminated(p, mc);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void sessionFailedToOpen(TransportProvider p, Throwable t) {
		if (jlog.isLoggable(Level.FINE)) {
			jlog.log(Level.FINE, "connectionFailedToOpen " + t.getMessage(), t);
		}
		for (SessionListener listener : this.abstractCommunication
				.getSlisteners()) {
			listener.sessionFailedToOpen(p, t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void sessionRecovered(TransportProvider p, Session mc) {
		if (jlog.isLoggable(Level.FINE)) {
			jlog.fine("connectionOverload " + mc);
		}
		for (SessionListener listener : this.abstractCommunication
				.getSlisteners()) {
			listener.sessionRecovered(p, mc);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void sessionStall(TransportProvider p, Session mc) {
		if (jlog.isLoggable(Level.FINE)) {
			jlog.fine("connectionStall " + mc);
		}
		for (SessionListener listener : this.abstractCommunication
				.getSlisteners()) {
			listener.sessionStall(p, mc);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void stateChangeComplete(TransportProvider p, Session session) {
		if (jlog.isLoggable(Level.FINE)) {
			jlog.fine("stateChangeComplete " + session);
		}
		for (SessionListener listener : this.abstractCommunication
				.getSlisteners()) {
			listener.stateChangeComplete(p, session);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void connectionLost(TransportProvider p, NioSession mc) {
		if (jlog.isLoggable(Level.FINE)) {
			jlog.fine("connectionLost " + mc);
		}

		mc.stall();
	}

	/**
	 * {@inheritDoc}
	 */
	public void connectionFixed(TransportProvider p, NioSession mc) {
		if (jlog.isLoggable(Level.FINE)) {
			jlog.fine("connectionFixed " + mc);
		}

	}

	public void messageReceived(Session s, Message<?> nextMessage) {
		if (jlog.isLoggable(Level.FINE)) {
			jlog.fine("messageReceived " + nextMessage);
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
	// if (jlog.isLoggable(Level.FINE)) {
	// jlog.fine("messageArrived " + m);
	// }
	// for (MessageChannelListener listener : mlisteners) {
	// listener.messageArrived(sc, m);
	// }
	// }
	//
}