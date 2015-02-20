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
package de.serviceflow.nutshell.cl.intern.spi;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.SessionState;
import de.serviceflow.nutshell.cl.intern.Communication;
import de.serviceflow.nutshell.cl.intern.NioSession;
import de.serviceflow.nutshell.cl.intern.OperationDelegateTCP;
import de.serviceflow.nutshell.cl.intern.SessionObject;

/**
 * TCP implementation on server-side.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 2e7e36ae0301afa4d19601b604eabdf0c0f3ef1a $
 * 
 * 
 */
public class TCPService extends AbtractTransportService {

	private static final Logger jlog = Logger.getLogger(TCPService.class
			.getName());

	public void init(Communication c, InetSocketAddress isa) {
		super.init(c, isa);
		delegate.setNIOTransportProvider(this);
	}

	private final OperationDelegateTCP delegate = new OperationDelegateTCP();

	protected SelectableChannel createChannel(Selector selector)
			throws IOException {
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ServerSocket ss = ssc.socket();
		ss.bind(isa);

		register(ssc, SelectionKey.OP_ACCEPT);

		if (jlog.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
			jlog.log(SessionObject.MSG_TRACE_LEVEL, "TCPService started on port "
					+ isa.getPort());
		}

		return ssc;
	}

	public void terminate(NioSession communicationSession) {
		delegate.terminate(communicationSession);
	}

	protected void opAccept(SelectionKey key) throws IOException {

		// Accept the new connection
		ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
		if (getConnectionApprover().approve(this, ssc)) {
			SocketChannel sc = ssc.accept();

			InetAddress a = ((InetSocketAddress) sc.getRemoteAddress())
					.getAddress();
			if (isBlacklisted(a)) {
				if (jlog.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
					jlog.log(SessionObject.MSG_TRACE_LEVEL,
							"Reject Blacklisted: " + a);
				}
				return;
			}

			sc.configureBlocking(false);
			NioSession session = new SessionObject();
			session.setSessionState(SessionState.CREATED);
			session.open(// getApplicationProtocol(),
			this);
			session.setCommunication(getCommunication());

			// Add the new connection to the selector
			SelectionKey newkey = register(sc, SelectionKey.OP_READ
					| SelectionKey.OP_WRITE);
			newkey.attach(session);

			session.setKey(newkey);
			session.setChannel(sc);

			if (jlog.isLoggable(Level.FINE)) {
				jlog.fine("Got connection from " + sc);
			}
		} else {
			if (jlog.isLoggable(Level.FINE)) {
				jlog.fine("Rejected: " + ssc);
			}
		}
	}

	@Override
	protected void opRead(SelectionKey key) throws IOException {
		if (delegate.opRead(key)) {
			getProtocolListenerHelper().connectionLost(this,
					(NioSession) key.attachment());
			key.attach(null);
			key.cancel();
		}
	}

	@Override
	protected void opWrite(SelectionKey key) throws IOException {
		delegate.opWrite(key);
	}

	public int getMessagesSend() {
		return delegate.getMessagesSend();
	}

	public void setMessagesSend(int messagesSend) {
		delegate.setMessagesSend(messagesSend);
	}

	public int getMessagesReceived() {
		return delegate.getMessagesReceived();
	}

	public void setMessagesReceived(int messagesReceived) {
		delegate.setMessagesReceived(messagesReceived);
	}

}
