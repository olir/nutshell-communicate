/**
 * 
 */
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

import java.util.HashMap;
import java.util.Map;

import de.serviceflow.nutshell.cl.intern.SessionObject;
import de.serviceflow.nutshell.cl.intern.TransportProvider;

/**
 * Basic implementation for a SessionListener.
 * <p>
 * Implement
 * {@link SessionListener#sessionCreated(TransportProvider, SessionObject)} and
 * call {@link #addCommunicationSession(SessionObject, Object)}.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: $
 * 
 * 
 */
public class DefaultSessionListener implements SessionListener {

	private Map<Object, Session> sessions = new HashMap<Object, Session>();

	protected DefaultSessionListener() {
	}

	public Session getCommunicationSession(Object userObject) {
		return sessions.get(userObject);
	}

	protected final void addCommunicationSession(Session session, Object userObject) {
		sessions.put(userObject, session);
		session.setUserObject(userObject);
	}

	protected final void removeCommunicationSession(Session session) {
		Object userObject = session.getUserObject();
		session.setUserObject(null);
		sessions.remove(userObject);
	}

	public void sessionCreated(TransportProvider p, Session session) {

	}

	public void sessionTerminated(TransportProvider p, Session session) {
		removeCommunicationSession(session);
	}

	public void sessionFailedToOpen(TransportProvider p, Throwable t) {

	}

	public void connectionLost(TransportProvider p, Session session) {

	}

	public void sessionStale(TransportProvider p, Session session) {

	}

	public void sessionRecovered(TransportProvider p, Session session) {

	}

	public void stateChangeComplete(TransportProvider p, Session session) {

	}
}
