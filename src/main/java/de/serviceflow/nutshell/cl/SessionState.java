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
 * The state of the session. The application can use it when it is ACTIVE.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: $
 * 
 * 
 */
public enum SessionState {
	/**
	 * SessionObject has just been created and authentication sub protocol is running.
	 * Application messages are buffered but not processed until session gets active.
	 */
	CREATED, 
	
	/**
	 * SessionObject is active and application messages can be exchanged.
	 */
	ACTIVE, 
	

	/**
	 * SessionObject is synchronizing protocol state.
	 */
	SYNC, 

	/**
	 * SessionObject is stale and recovery sub protocol is running.
	 */
	STALE, 
	
	
	/**
	 * SessionObject has been terminated and no more communication is possible.
	 */
	TERMINATED
}
