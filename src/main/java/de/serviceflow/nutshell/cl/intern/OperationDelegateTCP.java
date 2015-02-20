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

import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.intern.util.Pipe;

public class OperationDelegateTCP {
	private static final Logger jlog = Logger
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
	public boolean opRead(SelectionKey key) throws IOException {

		SessionObject session = (SessionObject) key.attachment();
		SocketChannel sc = (SocketChannel) key.channel();

		ByteBuffer buffer = session.getReadBuffer();

		int count = sc.read(buffer);
		if (count < 0) {
			if (jlog.isLoggable(Level.FINE)) {
				jlog.fine("opRead: -1 => connection lost");
			}
			return true;
		}

		while (buffer.position() >= 2) { // enough data for getting size

			short size = buffer.getShort(0);
			if (buffer.position() < size + 2) {
				if (jlog.isLoggable(Level.FINE)) {
					jlog.fine("ReadBuffer not filled. pos=" + buffer.position()
							+ " of " + (size + 2));
				}
				return false; // not enough data yet to deserialize message.
			}

			if (jlog.isLoggable(Level.FINE)) {
				jlog.fine("opRead: buffer filled (size=" + size + ")");
			}

			buffer.flip();
			buffer.position(2); // skip size
			// no sessionkey required yet.
			
			// currently only 1 protocol supported per session
			// client and server id do not match - map it
			int controlType = buffer.get();
			if (controlType>0)
				controlType=session.getApplicationProtocol().getId();
//			jlog.warning("<<<<< controlType="+controlType);
			int commandId = buffer.get();
			Message<?> nextMessage = Message.requestMessage(commandId,
					controlType);
			if (jlog.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
				jlog.log(SessionObject.MSG_TRACE_LEVEL, "TCP.opRead: "
						+ nextMessage + " for " + session);
			}

			nextMessage.readObject(buffer);
			if (jlog.isLoggable(Level.FINE)) {
				jlog.fine("opRead: next deserialized: " + nextMessage);
			}

			buffer.compact();
			if (jlog.isLoggable(Level.FINE)) {
				jlog.fine("opRead: buffer compacted (position="
						+ buffer.position() + ")");
			}

			nextMessage.setSession(session);
			// session.receive(nextMessage);
			messagesReceived++;
			session.internMessageReceived(nextMessage, ts);
		}
		return false;
	}

	public void opWrite(SelectionKey key) throws IOException {

		SessionObject session = (SessionObject) key.attachment();
		ByteBuffer buffer;

		if (session.isWriteBufferFlushed()) {
			// clean - write next message in buffer ...

			Pipe<Message<?>> mcSendPipe = session.getOutgoingMessages(ts);
			if (mcSendPipe.isClean()) {
				return; // no message
			}

			Message<?> m = mcSendPipe.next();
			if (jlog.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
				jlog.log(SessionObject.MSG_TRACE_LEVEL, "TCP.opWrite: " + m
						+ " for " + session);
			}
			buffer = session.getWriteBuffer();
			buffer.clear();
			buffer.position(2);
			// buffer.putLong(session.getSessionkey());
			int controlType = m.getClassificationValue();
			// currently only 1 protocol supported per session
			// client and server id do not match - map it
			if (controlType>1)
				controlType = 1; 
			buffer.put((byte) controlType);
//			jlog.warning(">>>>> controlType="+m.getClassificationValue());
			buffer.put((byte) m.getCommandId());
			m.writeObject(buffer);
			int size = buffer.position() - 2;
			if (size > Short.MAX_VALUE) {
				jlog.severe("APPLICATION to large (" + size + "):" + m);
				m.releaseMessage();
				return;
			}
			buffer.putShort(0, (short) size);
			buffer.flip();

			session.setWriteBufferFlushed(false);
			if (jlog.isLoggable(Level.FINE)) {
				jlog.fine("opWrite: cached (size=" + size + ") for: " + m);

			}

			m.releaseMessage();
		} else {
			buffer = session.getWriteBuffer();
			if (jlog.isLoggable(Level.FINE)) {
				jlog.fine("WriteBuffer not Flushed. pos=" + buffer.position()
						+ " of " + buffer.limit());
			}
		}

		SocketChannel sc = (SocketChannel) key.channel();
		sc.write(buffer);
		if (buffer.position() < buffer.limit()) {
			if (jlog.isLoggable(Level.FINE)) {
				jlog.fine("WriteBuffer not Flushed. pos=" + buffer.position()
						+ " of " + buffer.limit());
			}
			return;
		}
		session.setWriteBufferFlushed(true);
		messagesSend++;
		if (jlog.isLoggable(Level.FINE)) {
			jlog.fine("opWrite: ok (limit=" + buffer.limit() + ")");
		}
	}

	public int getMessagesSend() {
		return messagesSend;
	}

	public void setMessagesSend(int messagesSend) {
		this.messagesSend = messagesSend;
	}

	public int getMessagesReceived() {
		return messagesReceived;
	}

	public void setMessagesReceived(int messagesReceived) {
		this.messagesReceived = messagesReceived;
	}

	/**
	 * 
	 */
	public void setNIOTransportProvider(NIOTransportProvider ts) {
		this.ts = ts;
	}

	public void terminate(NioSession communicationSession) {
		SelectionKey k = communicationSession.getKey();
		if (k != null && k.isValid()) {
			try {
				jlog.info("cancel key.");
				k.cancel();
			} catch (Exception e) {
			}
			communicationSession.setKey(null);
		}

		SelectableChannel c = communicationSession.getChannel();
		if (c != null && c.isOpen()) {
			try {
				jlog.info("closing channel.");
				c.close();
			} catch (IOException e) {
			}
			communicationSession.setChannel(null);
		}
	}

}
