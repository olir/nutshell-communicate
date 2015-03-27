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

import java.util.HashSet;
import java.util.Set;

import de.serviceflow.nutshell.cl.APMessage;
import de.serviceflow.nutshell.cl.APState;


/**
 * Helps with message registration,  protocol execution and validation. 
 */
public class MessageRegistryHelper {
	public static final MessageRegistryHelper DEFAULT = new MessageRegistryHelper();

	private Set<APState> states = new HashSet<APState>();
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
	public final void add(APState state, APMessage apMessage) {
		this.states.add(state);
		if (this.apMessage==null) {
			this.apMessage = apMessage;
		}
		else {
			if (this.apMessage.isReliable()!=apMessage.isReliable())
				throw new Error("configurations of multi-state message different: reliable");
		}
	}

	public final boolean belongsTo(APState a) {
		return states.contains(a);
	}

	public final boolean isReliable() {
		return apMessage == null || apMessage.isReliable();
	}
}
