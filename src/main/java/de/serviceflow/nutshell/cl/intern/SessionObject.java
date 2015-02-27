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

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.APState;
import de.serviceflow.nutshell.cl.ApplicationProtocol;
import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.Session;
import de.serviceflow.nutshell.cl.SessionState;
import de.serviceflow.nutshell.cl.intern.cpi.UDPClient;
import de.serviceflow.nutshell.cl.intern.session.ClientAuthentication;
import de.serviceflow.nutshell.cl.intern.session.MessageClassification;
import de.serviceflow.nutshell.cl.intern.session.SessionAccepted;
import de.serviceflow.nutshell.cl.intern.spi.AbtractTransportService;
import de.serviceflow.nutshell.cl.intern.spi.UDPService;
import de.serviceflow.nutshell.cl.intern.util.Pipe;

/**
 * A logical SESSION between client and server.
 * <p>
 * It is recoverable until the server drops it (timeout and sessionState
 * handling on client and server to be implemented by the application).
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 751c71037bb4e50563a30e47a16056dce73321e4 $
 * 
 * 
 */
public final class SessionObject implements Session, NioSession {
	private static final Logger JLOG = Logger.getLogger(SessionObject.class
			.getName());

	public static Level MSG_TRACE_LEVEL = Level.FINE;
	public static Level MSG_TRACE_LEVEL2 = Level.FINE;

	private Communication communication = null;
	private NIOTransportProvider mainProvider = null;
	private NIOTransportProvider unreliableProvider = null;
	private SelectableChannel channel = null;
	private SelectionKey key = null;

	public static final int BUFFER_SIZE = 1024;
	public static final int HEADER_SIZE = 3;

	private ByteBuffer writeBuffer = null;
	private ByteBuffer readBuffer = null;
	private boolean writeBufferFlushed = true;

	private final Pipe<Message> mainSendPipe = new Pipe<Message>(
			"name=SessionObject,hash=" + this.hashCode()
					+ ",direction=out.main");
	private final Pipe<Message> unreliableSendPipe = new Pipe<Message>(
			"name=SessionObject,hash=" + this.hashCode()
					+ ",direction=out.unreliable");
	

	private Object userObject = null;

	private SessionState sessionState = SessionState.CREATED;

	private ApplicationProtocol applicationProtocol = null;
	private APState protocolState;

	private Message nextMessage;
	private int contentLength;
	private SocketAddress address;

	private Principal user;

	private MessageBroker broker;

	private long sessionkey = 0L;

	/**
	 * 
	 */
	public SessionObject() {
	}

	NIOTransportProvider getProvider(boolean reliabel) {
		if (reliabel || unreliableProvider == null) {
			return mainProvider;
		} else {
			return unreliableProvider;
		}
	}

	public APState getApplicationProtocolState() {
		return protocolState;
	}


	public void setApplicationProtocolState(APState newState) {
		if (newState == null) {
			throw new IllegalStateException(
					"state null! named state not found?");
		}
		protocolState = newState;
		getProvider(true).changedApplicationProtocolState(this, newState);
	}

	public ApplicationProtocol getApplicationProtocol() {
		return applicationProtocol;
	}

	public String toString() {
		String id = (getApplicationProtocol() != null) ? String
				.valueOf(getApplicationProtocol().getId()) : "-";
		return "SessionObject(" + id + "|" + getProvider(true) + "/"
				+ getProvider(false) + ") #" + this.hashCode() + " key="
				+ sessionkey;
	}



	/**
	 * internal initialization.
	 * 
	 * @param sc
	 *            SocketChannel
	 */
	private void init() {
		// Create a direct buffer to get bytes of header from socket.
		if (writeBuffer == null) {
			readBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE + HEADER_SIZE
					+ 2);
			writeBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE + HEADER_SIZE
					+ 2);
		}
		readBuffer.clear();
		writeBuffer.clear();

		nextMessage = null;
		contentLength = 0;
		writeBufferFlushed = true;

		// attributes = new HashMap<String, Object>();
	}

	public void open(
	/* ApplicationProtocol protocol, */NIOTransportProvider provider) {
		this.mainProvider = provider;
		sessionkey = 0L; // non-defined
		init();

		if (provider instanceof AbtractTransportService) {
			broker = new ServerMessageBroker(this);
		} else {
			broker = new ClientMessageBroker(this);
		}
	}

	public void join(NIOTransportProvider unreliable) {
		this.unreliableProvider = unreliable;
	}


	/**
	 * used by TCP
	 */
	public Message getNextMessage() {
		return nextMessage;
	}

	/**
	 * used by TCP
	 */
	public void setNextMessage(Message nextMessage) {
		this.nextMessage = nextMessage;
	}

	/**
	 * used by TCP
	 */
	public int getContentLength() {
		return contentLength;
	}

	/**
	 * used by TCP
	 */
	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	public ByteBuffer getWriteBuffer() {
		if (JLOG.isLoggable(Level.FINEST)) {
			JLOG.finest("getWriteBuffer() remaining= "
					+ writeBuffer.remaining());
		}
		return writeBuffer;
	}

	public ByteBuffer getReadBuffer() {
		if (JLOG.isLoggable(Level.FINEST)) {
			JLOG.finest("getReadBuffer() remaining= " + readBuffer.remaining());
		}
		return readBuffer;
	}

	public Pipe<Message> getOutgoingMessages(NIOTransportProvider provider) {
		if (provider == mainProvider || unreliableProvider == null)
			return mainSendPipe;
		else
			return unreliableSendPipe;
	}

	// private Pipe<Message> getReceivedMessages() {
	// return receivePipe;
	// }

	public void receive(Message m) {
		// if (JLOG.isLoggable(Level.INFO)) {
		// JLOG.info("received on " + this + " " + m.getCommand() + ": " + m);
		// }
		// getReceivedMessages().add(m);
	}

	private void addMessageToSendPipe(Message m) {
		if (m.getMessageDefinition().isReliable() || unreliableProvider == null)
			mainSendPipe.add(m);
		else
			unreliableSendPipe.add(m);
	}

	/**
	 * 
	 * @param m
	 *            Message
	 */
	public final void internMessageReceived(Message m,
			NIOTransportProvider provider) {
		broker.broke(m, provider);
	}

	public void send(Message m) {
		if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
			JLOG.log(SessionObject.MSG_TRACE_LEVEL, "send on " + this + ": "
					+ m);
		}
		if (m.getProtocolId() < 0)
			throw new Error("getProtocolId negative: send on " + this + ": " + m);
		if (m.getCommandId()<0)
			throw new Error("getCommandId negative: send on " + this + ": " + m);

		if (sessionState == SessionState.ACTIVE) {
			addMessageToSendPipe(m);
		} else if (sessionState == SessionState.CREATED) {
			if (m.getProtocolId() == MessageClassification.SESSION
					.value()) {
				if (m instanceof ClientAuthentication) {
					ClientAuthentication a = ((ClientAuthentication) m);
					if (a.dualChannel && sessionkey != 0)
						unreliableSendPipe.add(m);
					else
						addMessageToSendPipe(m);
				} else if (m instanceof SessionAccepted) {
					// SessionAccepted a = ((SessionAccepted) m);
					if (isDualChannel() && sessionkey != 0)
						unreliableSendPipe.add(m);
					else
						addMessageToSendPipe(m);
				} else
					addMessageToSendPipe(m);
			} else {
				throw new IllegalStateException(
						"SessionObject is not active yet. Unexpected: " + m);
			}
		} else if (sessionState == SessionState.SYNC) {
			if (m.getProtocolId() == MessageClassification.SESSION
					.value()) {
				addMessageToSendPipe(m);
			} else {
				throw new IllegalStateException(
						"SessionObject is in synchronization. Unexpected: " + m);
			}
		} else if (sessionState == SessionState.TERMINATED) {
			throw new IllegalStateException(
					"SessionObject terminated! Unexpected: " + m);
		} else {
			throw new IllegalStateException("Message rejected in sessionState "
					+ sessionState + " send on " + this + ": " + m);
		}
	}

	// public Map<String, Object> getAttributes() {
	// return attributes;
	// }

	/**
	 * used by UDP
	 */
	public SocketAddress getAddress() {
		return address;
	}

	/**
	 * used by UDP
	 */
	public void setAddress(SocketAddress address) {
		this.address = address;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	public void setCommunication(Communication communication) {
		this.communication = communication;
	}

	public boolean isWriteBufferFlushed() {
		return writeBufferFlushed;
	}

	public void setWriteBufferFlushed(boolean writeBufferFlushed) {
		this.writeBufferFlushed = writeBufferFlushed;
	}

	public SessionState getSessionState() {
		return sessionState;
	}

	public void setSessionState(SessionState state) {
		this.sessionState = state;
		if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
			JLOG.log(SessionObject.MSG_TRACE_LEVEL,
					"SessionObject state changed for " + this
							+ ". ****************** New state: " + state);
		}
	}

	public long getSessionkey() {
		return sessionkey;
	}

	public boolean isDualChannel() {
		return unreliableProvider != null;
	}

	public Principal getPrincipal() {
		return user;
	}

	public void setPrincipal(Principal user) {
		this.user = user;
	}

	public void setSessionkey(long sessionkey) {
		this.sessionkey = sessionkey;
	}

	public boolean createDualChannel() {
		if (mainProvider instanceof UDPService
				|| mainProvider instanceof UDPClient)
			return false;
		unreliableProvider = mainProvider.join(this);
		return true;
	}

	public Communication getCommunication() {
		return communication;
	}

	public void terminate() {
		if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
			JLOG.log(SessionObject.MSG_TRACE_LEVEL, "terminating " + this);
		}
		setSessionState(SessionState.TERMINATED);
		mainProvider.terminate(this);
		if (unreliableProvider != null) {
			unreliableProvider.terminate(this);
		}
	}

	public boolean activate(String protocol) {
		if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
			JLOG.log(SessionObject.MSG_TRACE_LEVEL, "activating " + this
					+ ": '" + protocol + "'");
		}
		applicationProtocol = ApplicationProtocol.getByName(protocol);
		if (applicationProtocol != null) {
			this.protocolState = applicationProtocol.getInitialState();
			setSessionState(SessionState.ACTIVE);
		}
		return (applicationProtocol != null);
	}

	/**
	 * The session has gone stall.
	 */
	public void stall() {
		setSessionState(SessionState.STALE);
		// TODO mainProvider.reconnect(this);
	}

	public SelectableChannel getChannel() {
		return channel;
	}

	public void setChannel(SelectableChannel channel) {
		this.channel = channel;
	}

	public SelectionKey getKey() {
		return key;
	}

	public void setKey(SelectionKey key) {
		this.key = key;
	}

	public boolean inProtocolState(String key) {
		return getApplicationProtocolState() == getApplicationProtocol()
				.getState(key);
	}
}
