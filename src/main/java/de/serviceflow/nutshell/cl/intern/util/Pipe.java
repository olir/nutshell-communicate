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

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import de.serviceflow.nutshell.cl.intern.Communication;

/**
 * The Pipe class represents a first-in-first-out (FIFO) queue of objects,
 * intended to be used by multiple threads (consumers and providers). The
 * {@link #add(Object)} and {@link #next()} operations are thread-safe.
 * 
 * @param <T> Type to pool
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: a51938963048557dd1e0f61629579b7a147db525 $
 * 
 * 
 */
public class Pipe<T> implements PipeMBean<T> {
	private static Logger jlog = Logger.getLogger(Pipe.class.getName());

	/**
	 * internal data storage
	 * 
	 * @serial
	 */
	private Queue<T> queue = new ArrayDeque<T>();

	public Pipe(String mBeanONPostfix) {
		MBeanServer mBeanServer = Communication.getMbeanServer();
		if (mBeanServer != null && mBeanONPostfix != null) {
			ObjectName n;
			try {
				n = new ObjectName(Communication.MBEAN_PACKAGE + ":"
						+ "type=Pipe," + mBeanONPostfix);
				mBeanServer.registerMBean(this, n);
			} catch (MalformedObjectNameException
					| InstanceAlreadyExistsException
					| MBeanRegistrationException | NotCompliantMBeanException e) {
				jlog.log(Level.WARNING, e.toString(), e);
			}

		}
	}

	/**
	 * Adds an element into the pipe. This operation is thread-safe.
	 */
	public synchronized final void add(T o) {
		queue.add(o);
		if (jlog.isLoggable(Level.FINEST)) {
			jlog.finest("#ADD " + o + " to " + toString());
		}
	}

	/**
	 * Gets the next element from the pipe. This method exits immediately. If no
	 * element is in the pipe it returns null. This operation is thread-safe.
	 */
	public synchronized final T next() {
		if (queue.isEmpty())
			return null;
		T t = queue.remove();
		if (jlog.isLoggable(Level.FINEST)) {
			jlog.finest("#NEXT " + t + " from " + toString());
		}
		return t;
	}

	/**
	 * Test if the pipe is empty or not
	 */
	public final boolean isClean() {
		return queue.isEmpty();
	}

	/**
	 * Returns the number of elements in the pipe. This is commonly not equal to
	 * the size of the underlying vector.
	 */
	public final int length() {
		return queue.size();
	}

	public String toString() {
		return "Pipe " + this.hashCode();
	}

}
