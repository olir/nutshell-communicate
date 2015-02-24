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
import java.util.Map;

public class LookupRegistry {
	private static LookupRegistry r;
	
	private final Map<Class<?>, Lookup<?>> lookupMap = new HashMap<Class<?>, Lookup<?>>();
	
	public static LookupRegistry getInstance() {
		if (r ==  null) {
			r = new LookupRegistry();
		}
		return r;
	}
	
	public Lookup<?> getLookup(Class<?> type) {
		return lookupMap.get(type);
	}
	
	public void register(Class<?> type, Lookup<?> l) {
		lookupMap.put(type, l);
	}
}
