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
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.intern.Communication;
import de.serviceflow.nutshell.cl.intern.NioSession;
import de.serviceflow.nutshell.cl.intern.OperationDelegateUDP;

/**
 * UDP implementation on client-side.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 3c94a3919bef62623caf7bf1d507723f3d79c977 $
 * 
 * 
 */
public class UDPClient extends AbtractTransportClient {
	private static Logger JLOG = Logger.getLogger(UDPClient.class.getName());

	private final OperationDelegateUDP delegate = new OperationDelegateUDP();

	// @SuppressWarnings("unused")
	// private static final int IPTOS_LOWCOST = 0x02;
	// @SuppressWarnings("unused")
	// private static final int IPTOS_RELIABILITY = 0x04;
	// @SuppressWarnings("unused")
	// private static final int IPTOS_THROUGHPUT = 0x08;
	// @SuppressWarnings("unused")
	// private static final int IPTOS_LOWDELAY = 0x10;

	private DatagramChannel dc;

	public UDPClient(String applicationProtocolName) {
		super(applicationProtocolName);
	}
	
	/**
	 * A new UDP Client
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

	public final void terminate(NioSession communicationSession) {
		try {
			stop();
		} catch (IOException e) {
			JLOG.finest("terminate() - close failed.");
		}
	}

	protected SelectableChannel createChannel(Selector selector)
			throws IOException {

		if (JLOG.isLoggable(Level.FINER)) {
			JLOG.finer("createChannel() ....");
		}

		dc = DatagramChannel.open();
		dc.configureBlocking(false);

		/*
		 * setTrafficClass had none effect in tests. neither on reliability, no
		 * on delay.
		 */
		// DatagramSocket dgs =
		dc.socket();
		// dgs.setTrafficClass();

		authenticate(isa);

		register(dc, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

		return dc;
	}

	protected void authenticate(InetSocketAddress isa) {
		// Creates New SessionObject!
		authenticate(delegate.addSession(isa), getApplicationProtocolName());
	}

	public void addSessionAndAuthenticate(NioSession session) {
		delegate.addSession(session, isa);
		authenticate(session, getApplicationProtocolName());
	}

	@Override
	protected void opRead(SelectionKey key) throws IOException {
		delegate.opRead(key);
	}

	@Override
	protected void opWrite(SelectionKey key) throws IOException {
		delegate.opWrite(key);
	}

	public final int getMessagesSend() {
		return delegate.getMessagesSend();
	}

	public final void setMessagesSend(int messagesSend) {
		delegate.setMessagesSend(messagesSend);
	}

	public final int getMessagesReceived() {
		return delegate.getMessagesReceived();
	}

	public final void setMessagesReceived(int messagesReceived) {
		delegate.setMessagesReceived(messagesReceived);
	}

}
