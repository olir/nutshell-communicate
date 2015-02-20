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

/**
 * Pool with limited capacity.
 * @param <E> Type to pool
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: fbcfd2e7a099d4417e57756090446d0f9f3e5ef0 $
 * 
 * 
 */
public abstract class Bucket<E> extends Pool<E> {
	
	private final int capacity;

	public Bucket(int capacity) {
		this.capacity = capacity;
	}
	
	public void release(E e) {
		if (size()>=capacity) {
			toss(e);
		}
		else
			super.releaseElementToPool(e);
	}
	
	protected abstract void toss(E e);

	public int getCapacity() {
		return capacity;
	}
}
