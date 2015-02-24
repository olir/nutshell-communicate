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

import java.security.Principal;
import java.util.UUID;
import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.ApplicationProtocol;
import de.serviceflow.nutshell.cl.Authentication;
import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.ServerCommunication;
import de.serviceflow.nutshell.cl.intern.session.ClientAuthentication;
import de.serviceflow.nutshell.cl.intern.session.MessageClassification;
import de.serviceflow.nutshell.cl.intern.session.SessionAccepted;
import de.serviceflow.nutshell.cl.intern.session.SessionClosed;

public class ServerMessageBroker implements MessageBroker {
	private static final Logger JLOG = Logger
			.getLogger(ServerMessageBroker.class.getName());

	private final SessionObject session;

	private boolean created(Message m) {
		if (m instanceof ClientAuthentication) {
			ClientAuthentication ca = (ClientAuthentication) m;
			if (!checkAuthentication(ca)) {
				session.terminate();
				return false;
			}

			if (session.getSessionkey() == 0L) {
				String protocol = acceptAuthentication(0L,
						ca.requestedProtocol.toString()); // TCP

				if (ca.dualChannel) {
					if (!session.createDualChannel())
						return false;
				} else {
					if (!session.activate(protocol)) {
						return false;
					}
					session.getCommunication().getProtocolListenerHelper()
							.sessionCreated(session.getProvider(true), session);

				}
			} else {
				String protocol = acceptAuthentication(session.getSessionkey(),
						ca.requestedProtocol.toString()); // UDP

				if (!session.activate(protocol)) {
					return false;
				}
				session.getCommunication().getProtocolListenerHelper()
						.sessionCreated(session.getProvider(false), session);
			}

			return true;
		} else {
			return false;
		}
	}

	private String acceptAuthentication(long sessionkey,
			String requestedProtocol) {

		ApplicationProtocol p = ApplicationProtocol
				.getByName(requestedProtocol);
		if (p == null)
			throw new Error("Unupported protocol '" + requestedProtocol + "'"); // TODO
																				// improve
																				// this
																				// in

		if (sessionkey == 0L) {
			sessionkey = UUID.randomUUID().getMostSignificantBits();
			session.setSessionkey(sessionkey);
		}
		SessionAccepted mAccepted = (SessionAccepted) Message.requestMessage(
				SessionAccepted.class, MessageClassification.SESSION.value());
		mAccepted.sessionkey = sessionkey;
		mAccepted.nampVersion = 1;
		mAccepted.protocol.setString(p.getName());
		session.send(mAccepted);

		return p.getName();
	}

	private boolean checkAuthentication(ClientAuthentication ca) {
		Authentication a = ServerCommunication.getServerCommunication()
				.getAuthentication();
		Principal user = null;
		if (a != null) {
			user = a.authenticate(ca.credentials.getBytes());
		}
		if (user == null) {
			JLOG.info("Authentication denied.");
			// Remark: anonymous user should be implemented as
			// "Null Object".
			return false;
		}
		session.setPrincipal(user);
		return true;
	}

	private boolean sync(Message m) {
		return false;
	}

	private boolean stall(Message m) {
		return false;
	}

	private boolean active(Message m) {
		if (m.getProtocolId() == MessageClassification.SESSION.value()) {
			if (m instanceof SessionClosed) {
				JLOG.info("SessionObject closed by partner.");
				session.terminate();
				return true;
			}
		} else {
			session.getCommunication().getProtocolListenerHelper()
					.messageReceived(session, m);
			return true;
		}
		return false;
	}

	ServerMessageBroker(SessionObject session) {
		this.session = session;
	}

	public String toString() {
		return "Server-" + session;
	}

	/**
	 * 
	 * @param m
	 *            Message
	 */
	@Override
	public final void broke(Message m, NIOTransportProvider provider) {
		boolean noError = true;

		switch (session.getSessionState()) {
		case CREATED:
			if (m.getProtocolId() == MessageClassification.SESSION
					.value()) {
				noError = created(m);
			}
			break;
		case ACTIVE:
			noError = active(m);
			break;
		case STALE:
			noError = stall(m);
			break;
		case SYNC:
			if (m.getProtocolId() == MessageClassification.SESSION
					.value()) {
				noError = sync(m);
			}
			break;
		case TERMINATED:
			break;
		default:
			break;
		}

		if (!noError) {
			if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
				JLOG.log(SessionObject.MSG_TRACE_LEVEL, "broke(): ignored " + m
						+ " for " + this + ": message not expected in state "
						+ session.getSessionState());
			}
		} else {
			if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
				JLOG.log(SessionObject.MSG_TRACE_LEVEL,
						"broke(): consumed " + m + " for " + this + ". State: "
								+ session.getSessionState());
			}
		}
	}
}
