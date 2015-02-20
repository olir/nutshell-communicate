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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.APState;
import de.serviceflow.nutshell.cl.intern.spi.ConnectionApprover;

/**
 * Basic implementation for a TransportProvider based on java.nio.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: $
 * 
 * 
 */
public abstract class NIOTransportProvider implements TransportProvider {
	private static final Logger jlog = Logger
			.getLogger(NIOTransportProvider.class.getName());

	private Selector selector;
	private SelectableChannel sc;

	protected Communication communication;
	protected InetSocketAddress isa;
	// private ApplicationProtocol app;

	private ConnectionApprover ca = new DefaultConnectionApprover();

	private boolean running = false;

	private static Set<InetAddress> blacklist = new HashSet<InetAddress>();

	private static final Exception ACCESS_DENIED = new SecurityException(
			"Server denied access.");

	public String toString() {
		return getClass().getSimpleName();
	}

	protected void init(Communication c, InetSocketAddress isa) {
		communication = c;
		this.isa = isa;
	}

	public SessionListenerDispensor getProtocolListenerHelper() {
		return communication.getProtocolListenerHelper();
	}

	public Communication getCommunication() {
		return communication;
	}

	public void start() throws IOException {
		// Create a new selector
		selector = Selector.open();

		sc = createChannel(selector);

		this.running = true;
	}

	public void stop() throws IOException {
		this.running = false;
	}

	public void completeStop() throws IOException {
		if (sc != null && sc.isOpen()) {
			try {
				sc.close();
			} finally {
			}
		}
		sc = null;

		// Close selector
		if (selector != null && selector.isOpen()) {
			try {
				selector.close();
			} finally {
			}
		}
		selector = null;
	}

	/**
	 * true if nothing selected.
	 */
	public boolean process() throws IOException {
		if (selector == null || !selector.isOpen() || selector.select() == 0) {
			return true;
		}

		// if (newOperations != null)
		// operations.addAll(newOperations);

		Set<SelectionKey> selectedKeys = selector.selectedKeys();
		Iterator<SelectionKey> it = selectedKeys.iterator();

		while (it.hasNext()) {
			SelectionKey key = (SelectionKey) it.next();
			try {
				doOperation(key);
			} catch (IOException e) {
				NioSession session = (NioSession) key.attachment();
				key.cancel();
				jlog.log(Level.WARNING, "I/O Error - Key canceled.");
				getProtocolListenerHelper().connectionLost(this, session);
			} finally {
				it.remove();
			}
		}
		return false;
	}

	protected abstract SelectableChannel createChannel(Selector selector)
			throws IOException;

	private void doOperation(SelectionKey key) throws IOException {
		if (key.isValid() && key.isAcceptable()) {
			// jlog.info("ENTER opAccept()");
			opAccept(key);
			// jlog.info("LEAVE opAccept()");
		}
		if (key.isValid() && key.isReadable()) {
			// jlog.info("ENTER opRead()");
			opRead(key);
			// jlog.info("LEAVE opRead()");
		}
		if (key.isValid() && key.isWritable()) {
			// jlog.info("ENTER opWrite()");
			opWrite(key);
			// jlog.info("LEAVE opWrite()");
		}
		if (key.isValid() && key.isConnectable()) {
			// jlog.info("ENTER opConnect()");
			opConnect(key);
			// jlog.info("LEAVE opConnect()");
		}
		if (!key.isValid()) {
			NioSession session = (NioSession) key.attachment();
			jlog.log(Level.WARNING,
					"I/O Error - Key canceled (without exception).");
			if (session != null) {
				getProtocolListenerHelper().connectionLost(this, session);
			} else {
				getProtocolListenerHelper().sessionFailedToOpen(this,
						ACCESS_DENIED);
			}
		}

	}

	protected void opAccept(SelectionKey key) throws IOException {
	}

	protected void opRead(SelectionKey key) throws IOException {
	}

	protected void opWrite(SelectionKey key) throws IOException {
	}

	protected void opConnect(SelectionKey key) throws IOException {
	}

	protected final SelectionKey register(SelectableChannel channel,
			int operation) throws IOException {
		return channel.register(selector, operation);
	}

	public ConnectionApprover getConnectionApprover() {
		return ca;
	}

	public void setConnectionApprover(ConnectionApprover c) {
		if (c == null) {
			ca = new DefaultConnectionApprover();
		} else {
			ca = c;
		}
	}

	public boolean isRunning() {
		return running;
	}

	/**
	 * 
	 */
	private class DefaultConnectionApprover implements ConnectionApprover {
		public boolean approve(TransportProvider p, Object objective) {
			return true;
		}
	}

	public abstract void terminate(NioSession communicationSession);

	public abstract void changedApplicationProtocolState(SessionObject session,
			APState protocolState);

	/**
	 * 
	 * @param communicationSession
	 * @return
	 */
	public NIOTransportProvider join(SessionObject communicationSession) {
		jlog.severe("dualchannel: join() not implemented.");
		return null;
	}

	public static void blacklist(InetAddress address) {
		blacklist.add(address);
	}

	public static boolean isBlacklisted(InetAddress a) {
		return blacklist.contains(a);
	}

}