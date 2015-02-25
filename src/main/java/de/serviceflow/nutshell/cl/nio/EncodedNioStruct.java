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
package de.serviceflow.nutshell.cl.nio;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.intern.EncodedNioObjectIOHelper;

/**
 * Base class for objects using NIO Transfer as data structure.
 * 
 * TODO implement pooling for collections.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: fbcfd2e7a099d4417e57756090446d0f9f3e5ef0 $
 * 
 * 
 * @see NioObjectContainer
 */
public abstract class EncodedNioStruct implements Transferable {
	private static final Logger JLOG = Logger.getLogger(EncodedNioStruct.class
			.getName());

	private static final int AUTO_MAX = 1024;
	private static final int MIN = 128;
	private final EncodedNioObjectIOHelper ioHelper;

	private ByteBuffer buffer;

	protected EncodedNioStruct() {
		this(MIN);
	}

	protected EncodedNioStruct(int initialcapacity) {
		if (initialcapacity < 2) {
			initialcapacity = MIN;
		}
		buffer = ByteBuffer.allocateDirect(initialcapacity);

		ioHelper = EncodedNioObjectIOHelper.getInstance(this);
	}

	@SuppressWarnings("unused")
	private final void autoIncrease() {
		if (JLOG.isLoggable(Level.FINER)) {
			JLOG.finer("increase()");
		}
		int newLength = AUTO_MAX;
		if (newLength > AUTO_MAX)
			newLength = AUTO_MAX;

		ByteBuffer newbuffer = ByteBuffer.allocateDirect(newLength);
		buffer.flip();
		buffer = newbuffer.put(buffer);
	}

	@Override
	public final void readObject(ByteBuffer in) {
		ioHelper.readObject(this, in);
	}

	@Override
	public final void writeObject(ByteBuffer out) {
		ioHelper.writeObject(this, out);
	}

}
