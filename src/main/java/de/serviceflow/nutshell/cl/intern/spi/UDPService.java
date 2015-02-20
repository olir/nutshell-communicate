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
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.intern.Communication;
import de.serviceflow.nutshell.cl.intern.NioSession;
import de.serviceflow.nutshell.cl.intern.OperationDelegateUDP;
import de.serviceflow.nutshell.cl.intern.SessionObject;

/**
 * UDP implementation on server-side.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: d46b67f73383ebccdbc4942985528bafd916851e $
 * 
 * 
 */
public class UDPService extends AbtractTransportService {
	private static final Logger jlog = Logger.getLogger(UDPService.class
			.getName());

	protected final OperationDelegateUDP delegate = new OperationDelegateUDP();

	// @SuppressWarnings("unused")
	// private static final int IPTOS_LOWCOST = 0x02;
	// @SuppressWarnings("unused")
	// private static final int IPTOS_RELIABILITY = 0x04;
	// @SuppressWarnings("unused")
	// private static final int IPTOS_THROUGHPUT = 0x08;
	//
	// private static final int IPTOS_LOWDELAY = 0x10;

	@Override
	public void init(Communication c, InetSocketAddress isa) {
		super.init(c, isa);

		delegate.setNIOTransportProvider(this);
	}

	public void terminate(NioSession communicationSession) {
		delegate.terminate(communicationSession);
	}

	protected SelectableChannel createChannel(Selector selector)
			throws IOException {
		DatagramChannel dc = DatagramChannel.open();
		dc.configureBlocking(false);
		DatagramSocket ds = dc.socket();
		ds.bind(isa);

		// DatagramSocket dgs =
		dc.socket();
		// dgs.setTrafficClass(IPTOS_LOWDELAY);

		register(dc, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

		if (jlog.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
			jlog.log(SessionObject.MSG_TRACE_LEVEL, "UDPService started on port "
					+ isa.getPort());
		}

		return dc;
	}

	@Override
	protected void opRead(SelectionKey key) throws IOException {
		delegate.opRead(key);
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
