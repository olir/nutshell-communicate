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
package de.serviceflow.nutshell.cl.intern.session;

import de.serviceflow.nutshell.cl.Authentication;
import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.nio.NVarchar;

/**
 * The client send this message to authenticate to a server or proxy.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: $
 * 
 * 
 */
public class ClientAuthentication extends Message {
	public ClientAuthentication() {
		super(MessageClassification.SESSION.value());
	}
	
	public int requestedNampVersion;

	public final NVarchar requestedProtocol = new NVarchar();

	/**
	 * if 2nd channel the key of the accepted session. 0 otherwise.
	 */
	public long sessionkey;

	/**
	 * true: create dual-channel session
	 */
	public boolean dualChannel;

	/**
	 * Some data that meaning to an Authentication.
	 * 
	 * @see Authentication
	 */
	public final NVarchar credentials = new NVarchar();
}
