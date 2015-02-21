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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.SessionState;
import de.serviceflow.nutshell.cl.intern.util.Pipe;

public class OperationDelegateUDP {
	private static final Logger JLOG = Logger
			.getLogger(OperationDelegateUDP.class.getName());

	private Map<AddressKey, NioSession> runningSessions = Collections
			.synchronizedMap(new TreeMap<AddressKey, NioSession>());

	private int messagesSend = 0;
	private int messagesReceived = 0;

	private ByteBuffer buffer = ByteBuffer
			.allocateDirect(SessionObject.BUFFER_SIZE + SessionObject.HEADER_SIZE);

	private NIOTransportProvider ts;

	public final void opRead(SelectionKey key) throws IOException {

		DatagramChannel dc = (DatagramChannel) key.channel();
		buffer.clear();

		InetSocketAddress sa = (InetSocketAddress) dc.receive(buffer);
		if (NIOTransportProvider.isBlacklisted(sa.getAddress())) {
			if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
				JLOG.log(SessionObject.MSG_TRACE_LEVEL, "Reject Blacklisted: "
						+ sa.getAddress());
			}
			return;
		}
		buffer.flip();

		long sessionkey = buffer.getLong();

		NioSession session = findSession(sa, sessionkey);
		if (session == null) {
			if (ts != null && ts.getConnectionApprover().approve(ts, sa)) {
				session = addSession(sa);
				session.setSessionState(SessionState.CREATED);
			} else {
				JLOG.info("Rejected: " + sa);
				return;
			}
		}

		
		// currently only 1 protocol supported per session
		// client and server id do not match - map it
		int controlType = buffer.get();
		if (controlType>0)
			controlType=session.getApplicationProtocol().getId();
//		JLOG.warning("<<<<< controlType="+controlType);

		int commandId = buffer.get();
		Message<?> nextMessage = Message.requestMessage(commandId, controlType);
		if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
			JLOG.log(SessionObject.MSG_TRACE_LEVEL, "UDP.opRead: " + nextMessage
					+ " for " + session + " @" + sa);
		}
		nextMessage.readObject(buffer);

		nextMessage.setSession(session);
		messagesReceived++;
		session.internMessageReceived(nextMessage, ts);
	}

	public SessionObject addSession(InetSocketAddress sa) {
		SessionObject session;
		session = new SessionObject();
		session.setSessionState(SessionState.CREATED);
		session.open(// ts.getApplicationProtocol(),
				 ts);
		session.setCommunication(ts.getCommunication());
		AddressKey ak = new AddressKey(sa);
		if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
			JLOG.log(SessionObject.MSG_TRACE_LEVEL, "UDP: New AddressKey: " + sa
					+ ": " + ak + " on " + session);
		}
		runningSessions.put(ak, session);
		return session;
	}

	public void addSession(NioSession session, InetSocketAddress sa) {
		AddressKey ak = new AddressKey(sa);
		if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
			JLOG.log(SessionObject.MSG_TRACE_LEVEL, "UDP: Joined with Address "
					+ sa + ": " + ak + " on " + session);
		}
		runningSessions.put(ak, session);
	}

	public void opWrite(SelectionKey key) throws IOException {
		// JLOG.info("**** opWrite(1) **** "+this.ts+": "+runningSessions.size());

		DatagramChannel dc = (DatagramChannel) key.channel();

		for (Entry<AddressKey, NioSession> es : runningSessions.entrySet()) {
			NioSession session = es.getValue();

			// JLOG.info("opWrite ..."+session);

			Pipe<Message<?>> mcSendPipe = session.getOutgoingMessages(ts);
			if (mcSendPipe.isClean()) {
				continue; // no message
			}

			Message<?> m = mcSendPipe.next();
			if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
				JLOG.log(SessionObject.MSG_TRACE_LEVEL, "UDP.opWrite: " + m
						+ " for " + session + " @"
						+ es.getKey().getSocketAddress());
			}
			buffer.clear();
			buffer.putLong(session.getSessionkey());
			int controlType = m.getClassificationValue();
			// currently only 1 protocol supported per session
			// client and server id do not match - map it
			if (controlType>1)
				controlType = 1; 
			buffer.put((byte) controlType);
//			JLOG.warning(">>>>> controlType="+m.getClassificationValue());
			buffer.put((byte) m.getCommandId());
			m.writeObject(buffer);
			buffer.flip();

			if (dc.send(buffer, es.getKey().getSocketAddress()) > 0) {
				messagesSend++;
			} else {
				JLOG.severe("APPLICATION to large " + m);
			}
			m.releaseMessage();
		}
	}

	public final int getMessagesSend() {
		return messagesSend;
	}

	public final void setMessagesSend(int messagesSend) {
		this.messagesSend = messagesSend;
	}

	public final int getMessagesReceived() {
		return messagesReceived;
	}

	public final void setMessagesReceived(int messagesReceived) {
		this.messagesReceived = messagesReceived;
	}

	/**
	 * 
	 */
	public final void setNIOTransportProvider(NIOTransportProvider ts) {
		this.ts = ts;
	}

	NioSession findSession(InetSocketAddress a, long sessionkey) {
		if (sessionkey != 0) {
			for (Entry<AddressKey, NioSession> en : runningSessions.entrySet()) {
				if (sessionkey == en.getValue().getSessionkey()) {
					AddressKey key = en.getKey();
					NioSession value = en.getValue();
					if (value.getAddress() == null) {
						if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
							JLOG.log(SessionObject.MSG_TRACE_LEVEL,
									"findSession: Attaching Address to session "
											+ a + " @" + value);
						}
						runningSessions.remove(key);
						key.setSocketAddress(a); // attach sender
						runningSessions.put(key, value);
					} else {
						if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
							JLOG.log(SessionObject.MSG_TRACE_LEVEL,
									"findSession: SessionObject already has Address "
											+ key.getSocketAddress() + " @"
											+ value);
						}
					}
					return value;
				} else {
					if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
						JLOG.log(SessionObject.MSG_TRACE_LEVEL,
								"findSession: ------ info: " + a + " <=> "
										+ en.getKey().getSocketAddress());
						JLOG.log(SessionObject.MSG_TRACE_LEVEL,
								"findSession: ------ compared: " + sessionkey
										+ " <=> "
										+ en.getValue().getSessionkey());
					}
				}
			}
			JLOG.warning("Possible intrusion detected. UDP client from "
					+ a.getAddress() + " has unkown session key: " + sessionkey
					+ ". Blacklisted.");
			NIOTransportProvider.blacklist(a.getAddress());
			return null;
		} else {
			for (Entry<AddressKey, NioSession> en : runningSessions.entrySet()) {
				if (a.equals(en.getKey().getSocketAddress())) {
					if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
						JLOG.log(SessionObject.MSG_TRACE_LEVEL,
								"findSession: SessionObject without key: " + a + " @"
										+ en.getValue());
					}
					return en.getValue();
				} else {
					if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
						JLOG.log(SessionObject.MSG_TRACE_LEVEL,
								"findSession: ------ compared: " + a + " <=> "
										+ en.getKey().getSocketAddress());
					}
				}
			}
		}
		if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
			JLOG.log(SessionObject.MSG_TRACE_LEVEL,
					"findSession: no session found for " + a + " #"
							+ sessionkey);
		}
		return null;
	}

	public void terminate(NioSession communicationSession) {
		// TODO remove session
	}

}
