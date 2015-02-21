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
package de.serviceflow.nutshell.cl.intern.util;

import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Object Pool does object pooling :)
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: fbcfd2e7a099d4417e57756090446d0f9f3e5ef0 $
 * 
 * 
 */
public abstract class Pool<T> {
	private static Logger JLOG = Logger.getLogger(Pool.class.getName());

	/**
	 * internal data storage
	 * 
	 * @serial
	 */
	private Stack<T> stack;

	protected Pool() {
		stack = new Stack<T>();
	}

	protected Pool(Stack<T> s) {
		stack = s;
	}

	/**
	 * Get element from pool or create new instance if necessary.
	 */
	public final T requestElementFromPool() {
		if (stack.isEmpty()) {
			T t = newInstance();
			if (JLOG.isLoggable(Level.FINEST)) {
				JLOG.finest("#NEW " + t + " for " + toString());
			}
			return t;
		} else {
			T t = stack.pop();
			if (JLOG.isLoggable(Level.FINEST)) {
				JLOG.finest("#POP " + t + " from " + toString());
			}
			return t;
		}
	}

	/**
	 */
	public final void releaseElementToPool(T o) {
		if (JLOG.isLoggable(Level.FINEST)) {
			JLOG.finest("#PUSH " + o + " to " + toString());
		}
		stack.push(o);
	}

	/**
	 */
	protected abstract T newInstance();
	
	public String toString() {
		return "Pool " + this.hashCode();
	}

	public int size() {
		return stack.size();
	}
	
}
