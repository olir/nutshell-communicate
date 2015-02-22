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

import javax.management.ObjectName;

import de.serviceflow.nutshell.cl.intern.Communication;
import de.serviceflow.nutshell.cl.intern.NIOTransportProvider;
import de.serviceflow.nutshell.cl.intern.SessionObject;
import de.serviceflow.nutshell.cl.intern.session.NampTCPService;

/**
 * Server side communication class. used to startup communication.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 80377d58e107ce3c16b892482e2a9d8188e86106 $
 * 
 * 
 */
public class ServerCommunication extends Communication {
	private static Logger JLOG = Logger.getLogger(ServerCommunication.class
			.getName());

	private static ServerCommunication serverCommunication;

	private boolean running = true;
	private boolean shutdown = false;

	private Authentication authentication = null;

	/**
	 * Synchronization object
	 */
	private final List<NIOTransportProvider> providerList = Collections
			.synchronizedList(new ArrayList<NIOTransportProvider>());

	private final List<NIOTransportProvider> newProviderList = Collections
			.synchronizedList(new ArrayList<NIOTransportProvider>());

	/**
	 * Instance Access.
	 * 
	 * @param mBeanServer
	 * 
	 * @return ServerCommunication
	 */
	public static ServerCommunication getServerCommunication() {

		if (serverCommunication == null) {
			serverCommunication = new ServerCommunication();
		}
		return serverCommunication;
	}

	private ServerCommunication() {
		Thread t = new Thread(getThreadGroup(), new CommunicationLoop(),
				"ServerCommunicationThread");
		t.start();
	}

	/**
	 * Call once for each protocol/port.
	 * 
	 * @param isa
	 *            InetSocketAddress
	 * @param netProtocol
	 *            Protocol
	 * @param appProtocol
	 *            Protocol
	 * @return TransportService
	 * @throws IOException
	 */
	public NIOTransportProvider bind(InetSocketAddress isa
	// , ApplicationProtocol appProtocol
	) throws IOException {

		if (shutdown) {
			throw new IllegalStateException("Shutdown/Terminated");
		}

		NampTCPService service = new NampTCPService();
		service.init(this, isa // , appProtocol
		);

		if (Communication.getMbeanServer() != null) {
			try {
				ObjectName serviceName = new ObjectName(
						"de.serviceflow.nutshell.cl:type=AbtractTransportService,name="
								+ "NAMP");
				Communication.getMbeanServer().registerMBean(service,
						serviceName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		service.start();
		newProviderList.add(service);

		return service;
	}

	public void stopService(NIOTransportProvider service) throws IOException {
		if (service == null) {
			return;
		}

		service.stop();
	}

	public void stopAllServices() throws IOException {
		for (NIOTransportProvider service : providerList) {
			service.stop();
		}

		shutdown = true;
	}

	public Authentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Authentication authenticator) {
		this.authentication = authenticator;
	}

	private class CommunicationLoop implements Runnable {
		public void run() {
			for (; running;) {
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
						JLOG.log(Level.SEVERE,
								"Exception in communication Loop", t);
					}
				}
				if (toBeStopped != null) {
					providerList.remove(toBeStopped);
					toBeStopped = null;
					if (providerList.isEmpty() && shutdown) {
						running = false; // terminated.
					}
				}
				if (!newProviderList.isEmpty()) {
					synchronized (newProviderList) {
						providerList.addAll(newProviderList);
						newProviderList.clear();
					}
				}
				if (providerList.isEmpty() && communicationWorkers.isEmpty()) {
					sleep();
				}
				for (Runnable r : communicationWorkers) {
					try {
						r.run();
					} catch (Throwable t) {
						JLOG.log(Level.SEVERE,
								"Exception in communication Loop", t);
					}
				}
			}
			removeAllListeners();
			serverCommunication = null;
			if (JLOG.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
				JLOG.log(SessionObject.MSG_TRACE_LEVEL,
						"Finished server communication thread.");
			}
		}

		private void sleep() {
			try {
				Thread.sleep(1L);
			} catch (InterruptedException e) {
				running = false;
				JLOG.log(Level.SEVERE, "Exception in communication Loop", e);
			}

		}
	}

}