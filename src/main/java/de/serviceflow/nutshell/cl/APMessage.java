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

import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.intern.MessageRegistryHelper;

/**
 * Application Protocol Message XML helper.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 751c71037bb4e50563a30e47a16056dce73321e4 $
 * 
 * 
 */
public final class APMessage {
	@SuppressWarnings("unused")
	private static final Logger JLOG = Logger.getLogger(APMessage.class
			.getName());

	private Class<?> messageClass;
	private boolean reliable = true;

	APMessage(de.serviceflow.nutshell.cl.xml.MessageSpec xMessage,
			ApplicationProtocol protocol, APState parent)
			throws ClassNotFoundException, IllegalAccessException {
		String className = xMessage.getClassname();
		if (className.indexOf('.') < 0) {
			className = protocol.getMessageClassPackage() + '.' + className;
		}
		messageClass = Class.forName(className);
		if (!de.serviceflow.nutshell.cl.Message.class
				.isAssignableFrom(messageClass)) {
			throw new Error("APPLICATION class " + className
					+ " is not subclass of Message.");
		}

		reliable = xMessage.isReliable();

		MessageRegistryHelper def = de.serviceflow.nutshell.cl.Message
				.register(messageClass, ApplicationProtocol.getProtocolCount());
		def.add(parent, this);
	}

	public boolean isReliable() {
		return reliable;
	}

	public String toString() {
		return "APMessage[" + messageClass.getSimpleName() + "]";
	}

}