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

import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.APState;
import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.SessionState;
import de.serviceflow.nutshell.cl.intern.cpi.AbtractTransportClient;
import de.serviceflow.nutshell.cl.intern.session.ChangeState;
import de.serviceflow.nutshell.cl.intern.session.MessageClassification;
import de.serviceflow.nutshell.cl.intern.session.SessionAccepted;
import de.serviceflow.nutshell.cl.intern.session.SessionClosed;

public class ClientMessageBroker implements MessageBroker {
	private static final Logger JLOG = Logger
			.getLogger(ClientMessageBroker.class.getName());

	private final SessionObject session;

	private boolean created(Message m, NIOTransportProvider provider) {
		if (m instanceof SessionAccepted) {
			if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
				JLOG.log(SessionObject.MSG_TRACE_LEVEL,
						"key = " + session.getSessionkey());
			}
			boolean isUDP = session.getSessionkey() != 0;

			if (!isUDP)
				session.setSessionkey(((SessionAccepted) m).sessionkey);

			((AbtractTransportClient) provider).sessionAccepted(session);

			if (isUDP || !session.isDualChannel()) {
				if (!session
						.activate(((SessionAccepted) m).protocol.toString())) {
					return false;
				}
				session.getCommunication()
						.getProtocolListenerHelper()
						.sessionCreated(
								session.getProvider(!session.isDualChannel()),
								session);
			}
			return true;
		} else {
			return false;
		}
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
				if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
					JLOG.log(SessionObject.MSG_TRACE_LEVEL,
							"SessionObject closed by partner.");
				}
				session.terminate();
				return true;
			} else if (m instanceof ChangeState) {
				if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
					JLOG.log(SessionObject.MSG_TRACE_LEVEL,
							"SessionObject state change starting.");
				}
				session.setSessionState(SessionState.SYNC);
				session.setApplicationProtocolState(APState
						.get(((ChangeState) m).stateValue));
				return true;
			}
		} else {
			if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
				JLOG.log(SessionObject.MSG_TRACE_LEVEL, "forwarding " + m);
			}
			session.getCommunication().getProtocolListenerHelper()
					.messageReceived(session, m);
			return true;
		}
		return false;
	}

	ClientMessageBroker(SessionObject session) {
		this.session = session;
	}

	public String toString() {
		return "Client-" + session;
	}

	/**
	 * 
	 * @param m
	 *            Message
	 */
	@Override
	public final void broke(Message m, NIOTransportProvider provider) {
		boolean noError = true;
		if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
			JLOG.log(SessionObject.MSG_TRACE_LEVEL, "broking " + m);
		}
		switch (session.getSessionState()) {
		case CREATED:
			if (m.getProtocolId() == MessageClassification.SESSION
					.value()) {
				noError = created(m, provider);
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
		}
	}

}
