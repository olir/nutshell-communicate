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


/**
 * Listener for any messages that are received or have been send.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: $
 * 
 * 
 */
public interface MessageListener {
	/**
	 * Reports about a message that just have been arrived and needs to be
	 * handled (otherwise it is lost).
	 * <p>
	 * After the message has been consumed, it is required to give it back to
	 * the message pool by invoking {@link APPLICATION#releaseMessage()}.
	 * 
	 * @param s
	 *            the SessionObject
	 * @param m
	 *            the APPLICATION
	 */
	void messageReceived(Session s, Message<?> m);
}
