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
package de.serviceflow.nutshell.cl.intern.cpi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.SessionState;
import de.serviceflow.nutshell.cl.intern.Communication;
import de.serviceflow.nutshell.cl.intern.NioSession;
import de.serviceflow.nutshell.cl.intern.OperationDelegateTCP;
import de.serviceflow.nutshell.cl.intern.SessionObject;

/**
 * TCP implementation on client-side.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: d6ed348dc8b8d00f5727b1a752e62e28cdbcf25c $
 * 
 * 
 */
public class TCPClient extends AbtractTransportClient {
	private static final Logger jlog = Logger.getLogger(TCPClient.class
			.getName());

	private final OperationDelegateTCP delegate = new OperationDelegateTCP();

	private SocketChannel nsc;
	private SelectionKey key;

	public TCPClient(String applicationProtocolName) {
		super(applicationProtocolName);
	}
	
	/**
	 * A new TCPClient
	 * 
	 * @param c
	 *            the Communication
	 * @param isa
	 *            an InetSocketAddress
	 * @param p
	 *            the ApplicationProtocol
	 * @param credentials
	 */
	@Override
	public void init(Communication c, InetSocketAddress isa,
			byte[] credentials) {
		super.init(c, isa, credentials);
		delegate.setNIOTransportProvider(this);
	}

	protected OperationDelegateTCP getDelegate() {
		return delegate;
	}

	protected SelectableChannel createChannel(Selector selector)
			throws IOException {

		if (jlog.isLoggable(Level.FINER)) {
			jlog.finer("createChannel() ....");
		}

		nsc = SocketChannel.open();
		nsc.configureBlocking(false);

		/*
		 * Turning TcpNoDelay "true" seems to have no effect for single messages
		 * while it had bad results under load (even more delay). So i keep it
		 * at default.
		 */
		// nsc.socket().setTcpNoDelay(true);

		if (nsc.connect(isa)) {
			if (jlog.isLoggable(Level.FINER)) {
				jlog.finer("direct connect successfull");
			}
			NioSession session = new SessionObject();
			session.setSessionState(SessionState.CREATED);
			session.open(// getApplicationProtocol(),
			this);
			session.setCommunication(getCommunication());

			key = register(nsc, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			key.attach(session);

			authenticate(session, getApplicationProtocolName());
		} else {
			register(nsc, SelectionKey.OP_CONNECT);
		}

		return nsc;

	}

	public void terminate(NioSession communicationSession) {
		try {
			try {
				if (key != null && key.isValid()) {
					key.cancel();
				}
			} finally {
			}
			key = null;

			try {
				if (nsc != null && nsc.isOpen()) {
					nsc.close();
				}
			} finally {
			}
			nsc = null;

			stop();
		} catch (IOException e) {
			jlog.finest("terminate() - close failed.");
		}
	}

	protected void opConnect(SelectionKey acceptKey) throws IOException {
		SocketChannel sc = (SocketChannel) acceptKey.channel();
		try {
			if (sc.finishConnect()) {
				// jlog.info("finishConnect() successfull");
				NioSession session = new SessionObject();
				session.setSessionState(SessionState.CREATED);
				session.open(// getApplicationProtocol(),
				this);
				session.setCommunication(getCommunication());
				key = register(sc, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				key.attach(session);

				authenticate(session, getApplicationProtocolName());
			} else {
				jlog.severe("finishConnect() failed - Exception excpected.");
			}
		} catch (IOException x) {
			getProtocolListenerHelper().sessionFailedToOpen(this, x);
			if (jlog.isLoggable(Level.FINER)) {
				jlog.finest("finishConnect() failed");
			}
			// no event.
			try {
				sc.close();
			} catch (IOException x2) {
			}
		}
	}

	@Override
	protected void opRead(SelectionKey key) throws IOException {
		if (getDelegate().opRead(key)) {
			getProtocolListenerHelper().connectionLost(this,
					(NioSession) key.attachment());
			key.attach(null);
			key.cancel();
		}
	}

	@Override
	protected void opWrite(SelectionKey key) throws IOException {
		getDelegate().opWrite(key);
	}

	public int getMessagesSend() {
		return getDelegate().getMessagesSend();
	}

	public void setMessagesSend(int messagesSend) {
		getDelegate().setMessagesSend(messagesSend);
	}

	public int getMessagesReceived() {
		return getDelegate().getMessagesReceived();
	}

	public void setMessagesReceived(int messagesReceived) {
		getDelegate().setMessagesReceived(messagesReceived);
	}

}
