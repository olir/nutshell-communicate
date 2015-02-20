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

import java.security.Principal;

/**
 * SessionObject as seen by the application.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 80377d58e107ce3c16b892482e2a9d8188e86106 $
 * 
 * 
 */
public interface Session {
	/**
	 * 
	 * @return the Principal
	 */
	Principal getPrincipal();

	/**
	 * 
	 * @param m
	 *            a Message
	 */
	void send(Message<?> m);

	/**
	 * The session may attach an object to a session, to make it easier to find
	 * information when a message is received.
	 * 
	 * @return attached application object
	 */
	Object getUserObject();

	/**
	 * The session may attach an object to a session, to make it easier to find
	 * information when a message is received.
	 * 
	 * @param userObject
	 *            attached application object
	 */
	void setUserObject(Object userObject);

	/**
	 * State of the session. This is not to be confused with the state of the
	 * application protocol.
	 * 
	 * @return SessionState
	 */
	SessionState getSessionState();

	/**
	 * Switch protocol to a new state. 
	 * @param state APState
	 */
	void setApplicationProtocolState(APState state);

	/**
	 * Gets current APState.
	 * @return APState
	 */
	APState getApplicationProtocolState();

	/**
	 * Gets ApplicationProtocol os session.
	 * @return ApplicationProtocol
	 */
	ApplicationProtocol getApplicationProtocol();

	/**
	 * Tests protocol state
	 * @param string expected state
	 * @return true if application protocol is in in expected state
	 */
	boolean inProtocolState(String string);
}
