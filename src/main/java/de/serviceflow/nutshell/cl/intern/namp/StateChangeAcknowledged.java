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

import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.nio.Transfer;

/**
 * The server sends this message to initiate a state change.
 *  
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: $
 * 
 * 
 */
public class StateChangeAcknowledged extends Message<SessionMessage> {
	public StateChangeAcknowledged() {
		super(SessionMessage.STATE_CHANGE_ACKNOWLEDGED,
				MessageClassification.SESSION.value());
	}

	@Transfer
	public int stateValue;
	
}
