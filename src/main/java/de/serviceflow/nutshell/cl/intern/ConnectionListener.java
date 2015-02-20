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


/**
 * Listener for a session's connection events. This is required to handle
 * undesired interrupts in TCP and HTTP transport.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: $
 * 
 * 
 */
public interface ConnectionListener {
	/**
	 * A connection has been lost or a reconnection attempt failed, the SESSION
	 * is still valid, but inactive (does not accept messages).
	 * 
	 * @param p
	 *            TransportProvider
	 * @param session
	 *            the SESSION, that got disconnected.
	 */
	void connectionLost(TransportProvider p, NioSession session);

	/**
	 * A lost connection has been reestablished and can be used again.
	 * 
	 * @param p
	 *            TransportProvider
	 * @param session
	 *            the SESSION, that got reconnected.
	 */
	void connectionFixed(TransportProvider p, NioSession session);

}
