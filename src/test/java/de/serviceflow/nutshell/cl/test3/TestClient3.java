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
package de.serviceflow.nutshell.cl.test3;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.MessageListener;
import de.serviceflow.nutshell.cl.NetworkProtocolType;
import de.serviceflow.nutshell.cl.Session;
import de.serviceflow.nutshell.cl.SimpleClient;

public class TestClient3 extends SimpleClient{
	public final void connect() throws UnknownHostException, IOException {
		addApplicationProtocol(createProtocolReader());
		connect(NetworkProtocolType.TCP_UDP,
				new InetSocketAddress(InetAddress.getLocalHost(), 10000),
				"test3/v1", "".getBytes(), this, new MyCom());
	}
	
	class MyCom implements MessageListener {
		@Override
		public void messageReceived(Session s, Message m) {
		}
	}

	private final Reader createProtocolReader() {
		return new InputStreamReader(getClass().getResourceAsStream(
				"/de/serviceflow/nutshell/cl/test/myprotocol.xml"));
	}
}
