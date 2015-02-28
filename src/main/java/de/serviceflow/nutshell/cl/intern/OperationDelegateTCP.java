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
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.ApplicationProtocol;
import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.intern.util.Pipe;

public class OperationDelegateTCP {
	private static final Logger JLOG = Logger
			.getLogger(OperationDelegateTCP.class.getName());

	private int messagesSend = 0;
	private int messagesReceived = 0;

	private NIOTransportProvider ts;

	/**
	 * do read operation.
	 * 
	 * @param key
	 *            SelectionKey
	 * @return problem occurred. connection lost
	 * @throws IOException
	 */
	public final boolean opRead(SelectionKey key) throws IOException {

		SessionObject session = (SessionObject) key.attachment();
		SocketChannel sc = (SocketChannel) key.channel();

		ByteBuffer buffer = session.getReadBuffer();

		int count = sc.read(buffer);
		if (count < 0) {
			if (JLOG.isLoggable(Level.FINE)) {
				JLOG.fine("opRead: -1 => connection lost");
			}
			return true;
		}

		while (buffer.position() >= 2) { // enough data for getting size

			short size = buffer.getShort(0);
			if (buffer.position() < size + 2) {
				if (JLOG.isLoggable(Level.FINE)) {
					JLOG.fine("ReadBuffer not filled. pos=" + buffer.position()
							+ " of " + (size + 2));
				}
				return false; // not enough data yet to deserialize message.
			}

			if (JLOG.isLoggable(Level.FINE)) {
				JLOG.fine("opRead: buffer filled (size=" + size + ")");
			}

			short controlType = buffer.get(2);
			if (controlType>0) {
				ApplicationProtocol protocol = session.getApplicationProtocol();
				if (protocol==null) {
					if (JLOG.isLoggable(Level.FINE)) {
						JLOG.fine("waiting for protocol to be set");
					}
					return false; // not in right state to deserialize.
				}
				// currently only 1 protocol supported per session
				// client and server id do not match - map it
				controlType=(short)protocol.getId();
			}
			
			buffer.flip();
			buffer.position(3); // skip size and control type
			// no sessionkey required yet.
			
			//			JLOG.warning("<<<<< controlType="+controlType);
			int commandId = buffer.get();
			Message nextMessage = Message.requestMessage(commandId,
					controlType);
			if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
				JLOG.log(SessionObject.MSG_TRACE_LEVEL, "TCP.opRead: "
						+ nextMessage + " for " + session);
			}

			nextMessage.readObject(buffer);
			if (JLOG.isLoggable(Level.FINE)) {
				JLOG.fine("opRead: next deserialized: " + nextMessage);
			}

			buffer.compact();
			if (JLOG.isLoggable(Level.FINE)) {
				JLOG.fine("opRead: buffer compacted (position="
						+ buffer.position() + ")");
			}

			nextMessage.setSession(session);
			// session.receive(nextMessage);
			messagesReceived++;
			session.internMessageReceived(nextMessage, ts);
		}
		return false;
	}

	public final void opWrite(SelectionKey key) throws IOException {

		SessionObject session = (SessionObject) key.attachment();
		ByteBuffer buffer;

		if (session.isWriteBufferFlushed()) {
			// clean - write next message in buffer ...

			Pipe<Message> mcSendPipe = session.getOutgoingMessages(ts);
			if (mcSendPipe.isClean()) {
				return; // no message
			}

			Message m = mcSendPipe.next();
			if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
				JLOG.log(SessionObject.MSG_TRACE_LEVEL, "TCP.opWrite: " + m
						+ " for " + session);
			}
			buffer = session.getWriteBuffer();
			buffer.clear();
			buffer.position(2);
			// buffer.putLong(session.getSessionkey());
			int controlType = m.getProtocolId();
			// currently only 1 protocol supported per session
			// client and server id do not match - map it
			if (controlType>1)
				controlType = 1; 
			else if (controlType<0)
				throw new Error("controlType: "+controlType); 
			buffer.put((byte) controlType);
//			JLOG.warning(">>>>> controlType="+m.getClassificationValue());
			int cid = m.getCommandId();
			if (cid<0)
				throw new Error("commandId: "+cid);			
			buffer.put((byte) cid);
			m.writeObject(buffer);
			int size = buffer.position() - 2;
			if (size > Short.MAX_VALUE) {
				JLOG.severe("APPLICATION to large (" + size + "):" + m);
				m.releaseMessage();
				return;
			}
			buffer.putShort(0, (short) size);
			buffer.flip();

			session.setWriteBufferFlushed(false);
			if (JLOG.isLoggable(Level.FINE)) {
				JLOG.fine("opWrite: cached (size=" + size + ") for: " + m);

			}

			m.releaseMessage();
		} else {
			buffer = session.getWriteBuffer();
			if (JLOG.isLoggable(Level.FINE)) {
				JLOG.fine("WriteBuffer not Flushed. pos=" + buffer.position()
						+ " of " + buffer.limit());
			}
		}

		SocketChannel sc = (SocketChannel) key.channel();
		sc.write(buffer);
		if (buffer.position() < buffer.limit()) {
			if (JLOG.isLoggable(Level.FINE)) {
				JLOG.fine("WriteBuffer not Flushed. pos=" + buffer.position()
						+ " of " + buffer.limit());
			}
			return;
		}
		session.setWriteBufferFlushed(true);
		messagesSend++;
		if (JLOG.isLoggable(Level.FINE)) {
			JLOG.fine("opWrite: ok (limit=" + buffer.limit() + ")");
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

	public final void terminate(NioSession communicationSession) {
		SelectionKey k = communicationSession.getKey();
		if (k != null && k.isValid()) {
			try {
				JLOG.info("cancel key.");
				k.cancel();
			} catch (Exception e) {
				JLOG.finest(e.toString());
			}
			communicationSession.setKey(null);
		}

		SelectableChannel c = communicationSession.getChannel();
		if (c != null && c.isOpen()) {
			try {
				JLOG.info("closing channel.");
				c.close();
			} catch (IOException e) {
				JLOG.finest(e.toString());
			}
			communicationSession.setChannel(null);
		}
	}

}
