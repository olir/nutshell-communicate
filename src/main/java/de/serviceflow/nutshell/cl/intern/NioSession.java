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

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.Session;
import de.serviceflow.nutshell.cl.SessionState;
import de.serviceflow.nutshell.cl.intern.util.Pipe;

/**
 * SessionObject as seen by Transport providers.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: $
 * 
 * 
 */
public interface NioSession extends Session {

	long getSessionkey();

	boolean isDualChannel();

	void open(
	// ApplicationProtocol applicationProtocol,
			NIOTransportProvider provider);

	void setSessionState(SessionState created);

	void setCommunication(Communication communication);

	Pipe<Message<?>> getOutgoingMessages(NIOTransportProvider provider);

	Object getAddress();

	void internMessageReceived(Message<?> nextMessage, NIOTransportProvider ts);

	void join(NIOTransportProvider udp);

	void stall();

	void setChannel(SelectableChannel sc);

	SelectableChannel getChannel();

	void setKey(SelectionKey newkey);

	SelectionKey getKey();

}
