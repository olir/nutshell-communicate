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

import de.serviceflow.nutshell.cl.APMessage;
import de.serviceflow.nutshell.cl.APState;


/**
 * Helps with message registration,  protocol execution and validation. 
 */
public class MessageRegistryHelper {
	// private static final Logger jlog =
	// Logger.getLogger(MessageRegistryHelper.class
	// .getName());

	public static final MessageRegistryHelper DEFAULT = new MessageRegistryHelper();

	private APState state = null;
	private APMessage apMessage = null;

	/**
	 * 
	 * @param state
	 *            register message for this state. A message may be registered
	 *            for multiple states by calling register() multiple times. May
	 *            be null in case of non-application messages.
	 * @param apMessage
	 *            definition of the message
	 */
	public void add(APState state, APMessage apMessage) {
		if (this.state != null) {
			throw new Error(
					"Message reuse in multipe states is not supported. Define different messages classes. apMessage:"
							+ apMessage
							+ ". state 1: "
							+ this.state
							+ ". state 2: " + state);
		}
		this.state = state;
		this.apMessage = apMessage;
	}

	public boolean belongsTo(APState a) {
		return state == null || a == state;
	}

	public boolean isReliable() {
		return apMessage == null || apMessage.isReliable();
	}

}
