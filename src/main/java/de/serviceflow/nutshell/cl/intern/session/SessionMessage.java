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

/**
 * Messages of the session layer in the NAMP. They belong to
 * {@link MessageClassification#SESSION}.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 64bda492741229e9e1dc5cf82eccacca4995caba $
 * 
 * 
 * 
 */
public enum SessionMessage {

	// All Messages need to be registered. It's done in AbstractCommunications.

	/**
	 * The client needs to authenticate first.
	 */
	CLIENT_AUTHENTICATION(0),

	/**
	 * If the client is a proxy he authenticates by this.
	 */
	PROXY_AUTHENTICATION(1),

	/**
	 * The Server responses to ClientAuthentication or ProxyAuthentication by
	 * this message, or AccessDenied.
	 */
	SESSION_ACCEPTED(2),

	/**
	 * The Server responses to ClientAuthentication or ProxyAuthentication he
	 * denies access by this message. When the Server closes the session
	 * (usually after a greater period of inactivity) he also tries to send this
	 * message.
	 */
	SESSION_CLOSED(3),

	/**
	 * Any state change is a synchronization point between server and client.
	 * The server initiates the change by this message. Any messages from the
	 * client are ignored until he accepts the change by
	 * STATE_CHANGE_ACKNOWLEDED.
	 */
	CHANGE_STATE(4),

	/**
	 * Any state change is a synchronization point between server and client.
	 * The clients acknowledges the change notification (CHANGE_STATE) by
	 * sending this message.
	 */
	STATE_CHANGE_ACKNOWLEDGED(5);

	private final int value;

	SessionMessage(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
