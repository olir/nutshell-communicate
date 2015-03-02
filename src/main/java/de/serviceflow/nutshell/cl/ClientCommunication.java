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
package de.serviceflow.nutshell.cl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.intern.Communication;
import de.serviceflow.nutshell.cl.intern.NIOTransportProvider;
import de.serviceflow.nutshell.cl.intern.cpi.AbtractTransportClient;
import de.serviceflow.nutshell.cl.intern.cpi.TCPClient;
import de.serviceflow.nutshell.cl.intern.cpi.UDPClient;
import de.serviceflow.nutshell.cl.intern.session.NampTCPClient;

/**
 * Represents the Communication Service on client-side. Obtained by
 * {@link ClientCommunication#getClientCommunication()}.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 64bda492741229e9e1dc5cf82eccacca4995caba $
 * 
 * 
 */
public class ClientCommunication extends Communication {
	private static Logger JLOG = Logger.getLogger(ClientCommunication.class
			.getName());

	private static ClientCommunication clientCommunication;

	private List<NIOTransportProvider> providerList = Collections
			.synchronizedList(new ArrayList<NIOTransportProvider>());

	private List<NIOTransportProvider> newProviderList = Collections
			.synchronizedList(new ArrayList<NIOTransportProvider>());

	private boolean shutdown = false;

	/**
	 * Default Instance Access. Use Constructor to create more instances.
	 * 
	 * @return ServerCommunication
	 */
	public static ClientCommunication getClientCommunication() {
		if (clientCommunication == null) {
			clientCommunication = new ClientCommunication();
		}
		return clientCommunication;
	}

	private ClientCommunication() {
		Thread comThread = new Thread(getThreadGroup(),
				new CommunicationLoop(), "CommunicationThread");
		comThread.start();
	}

	public NIOTransportProvider connect(NetworkProtocolType npt,
			InetSocketAddress isa, String applicationProtocolName,
			byte[] credentials) throws IOException {
		if (shutdown) {
			throw new IllegalStateException("Shutdown/Terminated");
		}

		AbtractTransportClient client;
		switch (npt) {
		case TCP_UDP:
			client = new NampTCPClient(applicationProtocolName);
			break;
		case TCP:
			client = new TCPClient(applicationProtocolName);
			break;
		case UDP:
			client = new UDPClient(applicationProtocolName);
			break;
		default:
			throw new IllegalArgumentException("protocol not supported: " + npt);
		}

		client.init(this, isa, credentials);

		client.start();
		newProviderList.add(client);

		return client;
	}

	public void stopClient(NIOTransportProvider client) throws IOException {
		if (client == null) {
			return;
		}

		client.stop();
	}

	public void stopAllClients() throws IOException {
		for (NIOTransportProvider client : providerList) {
			client.stop();
		}

		shutdown = true;
		removeAllListeners();
		clientCommunication = null;
	}

	protected void communicationStep() {
		setInCommunicationThread(true);
		NIOTransportProvider toBeStopped = null;
		for (NIOTransportProvider provider : providerList) {
			try {
				if (provider.isRunning()) {
					provider.process();
				} else {
					provider.completeStop();
					toBeStopped = provider;
					break; // restart loop instead fixing iterator.
				}
			} catch (Throwable t) {
				JLOG.log(Level.SEVERE, "Exception in communication Loop", t);
			}
		}
		if (toBeStopped != null) {
			providerList.remove(toBeStopped);
			toBeStopped = null;
		}
		if (!newProviderList.isEmpty()) {
			synchronized (newProviderList) {
				providerList.addAll(newProviderList);
				newProviderList.clear();
			}
		}
		for (Runnable r : communicationWorkers) {
			try {
				r.run();
			} catch (Throwable t) {
				JLOG.log(Level.SEVERE, "Exception in communication Loop", t);
			}
		}
		setInCommunicationThread(false);
	}

}
