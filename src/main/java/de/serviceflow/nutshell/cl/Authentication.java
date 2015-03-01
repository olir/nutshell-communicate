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

import java.security.Principal;


/**
 * Allows the application to handle the authentication. See
 * ServerCommunication.setAuthenticator(Authentication).
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 084ef77aa8b2b76d509025f57bccbf0aa03410ab $
 * 
 * 
 * @see ServerCommunication#setAuthentication(Authentication)
 */
public interface Authentication {
	/**
	 * do the authentication.
	 * 
	 * @param credentials
	 *            some data that allows the Authentication to make his decision.
	 * @param clientAddressObject Information about the underlying connection source.
	 * @return Principal if authenticated, return null if not.
	 */
	Principal authenticate(byte[] credentials, Object clientAddressObject);
}
