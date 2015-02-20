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
package de.serviceflow.nutshell.cl.intern.namp;

import java.io.IOException;
import java.net.InetSocketAddress;

import de.serviceflow.nutshell.cl.intern.Communication;
import de.serviceflow.nutshell.cl.intern.NIOTransportProvider;
import de.serviceflow.nutshell.cl.intern.SessionObject;
import de.serviceflow.nutshell.cl.intern.spi.TCPService;
import de.serviceflow.nutshell.cl.intern.spi.UDPService;

/**
 * Dual channel service (TCP + UDP).
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 64bda492741229e9e1dc5cf82eccacca4995caba $
 * 
 * 
 */
public class NampTCPService extends TCPService {

	private NampUDPService udp = new NampUDPService();

	@Override
	public NIOTransportProvider join(SessionObject communicationSession) {
		udp.join(communicationSession);

		// InetSocketAdress super.isa.getAddress();
		// // communicationSession.
		// AddressKey a = new AddressKey(sa);
		// // jlog.info("UDP: New AddressKey: " + a);
		// //
		//
		return udp;
	}

	@Override
	public void init(Communication c, InetSocketAddress isa) {
		super.init(c, isa);

		InetSocketAddress udpIsa = new InetSocketAddress(isa.getAddress(),
				isa.getPort() + 1);
		udp.init(c, udpIsa);

	}

	public void start() throws IOException {
		super.start();
		udp.start();
	}

	public void stop() throws IOException {
		super.stop();
		udp.stop();
	}

	public void completeStop() throws IOException {
		super.completeStop();
		udp.completeStop();
	}

	public boolean process() throws IOException {
		boolean hasNothingSelected1 = super.process();
		boolean hasNothingSelected2 = udp.process();
		return hasNothingSelected1 & hasNothingSelected2;
	}

	private class NampUDPService extends UDPService {
		/**
		 * 
		 * @param communicationSession
		 * @return
		 */
		public NIOTransportProvider join(SessionObject session) {
			delegate.addSession(session, null);
			return this;
		}

	}
}
