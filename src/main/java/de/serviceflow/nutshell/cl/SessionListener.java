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

import de.serviceflow.nutshell.cl.intern.TransportProvider;


/**
 * Listener for a session's transport events.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: $
 * 
 * 
 */
public interface SessionListener {
	/**
	 * A new logical connection has been created and is associated with a new
	 * MessageChannel (in initial state). Reported on client and server.
	 * 
	 * @param p
	 *            TransportProvider
	 * @param session
	 *            the new MessageChannel representing a logical connection
	 */
	void sessionCreated(TransportProvider p, Session session);

	/**
	 * Occurs when the try of a client to open a new session failed. Reported on
	 * client, only.
	 * 
	 * @param p
	 *            TransportProvider
	 * @param t
	 *            error
	 */
	void sessionFailedToOpen(TransportProvider p, Throwable t);

	/**
	 * A session has been finally terminated. It is closed and can no longer
	 * been used. Reported on client and server.
	 * 
	 * @param p
	 *            TransportProvider
	 * @param session
	 *            the SESSION, that gets invalid
	 */
	void sessionTerminated(TransportProvider p, Session session);

	/**
	 * A session has got stale, when no message arrived for some time, and can't
	 * be used until it is covered. Reported on client and server.
	 * 
	 * @param p
	 *            TransportProvider
	 * @param session
	 *            the SESSION, that got stall.
	 */
	void sessionStale(TransportProvider p, Session session);

	/**
	 * A session that got stall has been recovered and can be used again.
	 * 
	 * @param p
	 *            TransportProvider
	 * @param session
	 *            the SESSION, that got stall.
	 */
	void sessionRecovered(TransportProvider p, Session session);

	/**
	 * A session changed state. Server can again use session now.
	 * 
	 * @param p
	 *            TransportProvider
	 * @param session
	 *            the SESSION, that was in SYNC.
	 */
	void stateChangeComplete(TransportProvider p, Session session);

}
