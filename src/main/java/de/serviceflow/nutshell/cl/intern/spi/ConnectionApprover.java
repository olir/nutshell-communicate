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

import de.serviceflow.nutshell.cl.intern.TransportProvider;

/**
 * ConnectionApprover allows to control acceptance of new sessions.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 56305199fdd663984e01fd2d112a069ff0c901f2 $
 * 
 * 
 */
public interface ConnectionApprover {
	/**
	 * Tells the service if it should accept the transport-specific objective
	 * (e.g. a channel, SocketAdress, ...).
	 * 
	 * @param p
	 *            TransportProvider who requests a new connection
	 * @param objective
	 *            The objective for which a connection has to be aproved.
	 * @return the connection will be accepted if true
	 */
	boolean approve(TransportProvider p, Object objective);
}
