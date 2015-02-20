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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * State of application protocol. Application Protocol state XML helper.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 751c71037bb4e50563a30e47a16056dce73321e4 $
 * 
 * 
 */
public final class APState {

	private static Map<de.serviceflow.nutshell.cl.xml.State, APState> map = new HashMap<de.serviceflow.nutshell.cl.xml.State, APState>();

	public static APState get(de.serviceflow.nutshell.cl.xml.State xState) {
		return map.get(xState);
	}

	public static APState get(int stateValue) {
		for (APState s : map.values()) {
			if (s.value == stateValue)
				return s;
		}
		return null;
	}

	private static int count = 0;

	private final de.serviceflow.nutshell.cl.xml.State xState;
	private final APMessage messages[];
	private final int value;

	public APState(de.serviceflow.nutshell.cl.xml.State xState,
			ApplicationProtocol protocol) throws ClassNotFoundException,
			IllegalAccessException {
		value = count++;
		this.xState = xState;
		map.put(xState, this);

		List<de.serviceflow.nutshell.cl.xml.MessageSpec> xMessages = xState
				.getMessage();
		messages = new APMessage[xMessages.size()];
		int i = 0;
		for (de.serviceflow.nutshell.cl.xml.MessageSpec xMessage : xMessages) {
			messages[i++] = new APMessage(xMessage, protocol, this);
		}
	}

	public int value() {
		return value;
	}

	public String getName() {
		return xState.getName();
	}

	public APMessage[] getMessages() {
		return messages;
	}

	public String toString() {
		return "APState[" + getName() + "]";
	}
}