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

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.APState;
import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.SessionState;
import de.serviceflow.nutshell.cl.intern.Communication;
import de.serviceflow.nutshell.cl.intern.NIOTransportProvider;
import de.serviceflow.nutshell.cl.intern.SessionObject;
import de.serviceflow.nutshell.cl.intern.namp.ChangeState;
import de.serviceflow.nutshell.cl.intern.namp.MessageClassification;

/**
 * Base class for transport services.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 1493cedd0d579e3ea5101cd900a9c1517dec7c15 $
 * 
 * 
 */
public abstract class AbtractTransportService extends NIOTransportProvider
		implements TransportService, AbtractTransportServiceMBean {
	private static final Logger jlog = Logger
			.getLogger(AbtractTransportService.class.getName());

	@Override
	public void init(Communication c, InetSocketAddress a) {
		super.init(c, a);
	}

	public void changedApplicationProtocolState(SessionObject session,
			APState protocolState) {
		if (jlog.isLoggable(Level.FINE)) {
			jlog.fine("changedApplicationProtocolState [" + this
					+ "] switching to sessionState " + protocolState.getName());
		}
		session.setSessionState(SessionState.SYNC);

		ChangeState mChangeState = (ChangeState) Message.requestMessage(
				ChangeState.class, MessageClassification.SESSION.value());
		mChangeState.stateValue = protocolState.value();
		session.send(mChangeState);

	}

}
