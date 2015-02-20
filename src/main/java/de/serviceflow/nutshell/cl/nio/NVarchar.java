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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A String class replacement for messages.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: fbcfd2e7a099d4417e57756090446d0f9f3e5ef0 $
 * 
 * 
 */
public final class NVarchar implements Transferable {
	private static final Logger jlog = Logger.getLogger(NVarchar.class
			.getName());

	/**
	 * Limit size to avoid misuse or memory leaks.
	 */
	public static final int MAX = 1024;

	/**
	 * Limit minimum size to avoid misuse or performance impacts.
	 */
	public static final int MIN = 128;

	// private byte [] buffer;
	private ByteBuffer buffer;
	private int size = 0;

	public NVarchar() {
		this(MIN);
	}

	public NVarchar(int initialcapacity) {
		if (initialcapacity < 2) {
			initialcapacity = 2;
		}
		buffer = ByteBuffer.allocateDirect(initialcapacity);
		// buffer=new byte[initialcapacity];
	}

	public final int size() {
		return size;
	}

	public final byte getByte(int index) {
		if (index >= size) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return buffer.get(index);
		// return buffer[index];
	}

	public final void addByte(byte value) {
		// int l = buffer.length;
		int l = buffer.capacity();
		if (size == l) {
			increase();
		}
		// buffer[size++]=value;
		buffer.put(value);
	}

	private final void increase() {
		if (jlog.isLoggable(Level.FINER)) {
			jlog.finer("increase()");
		}
		// int newLength = buffer.length<<1;
		int newLength = MAX;
		if (newLength > MAX)
			newLength = MAX;

		// buffer = Arrays.copyOf(buffer, newLength);
		ByteBuffer newbuffer = ByteBuffer.allocateDirect(newLength);
		buffer.flip();
		buffer = newbuffer.put(buffer);
	}

	public final void addBytes(byte[] values) {
		// for (int i=0; i<values.length; i++)
		// addByte(values[i]);
		buffer.put(values);
	}

	/**
	 * Utility method for client-side implementation.
	 * 
	 * @param value
	 *            a String to add.
	 * @param charsetName
	 *            name of charset for value. if null it default to UTF-8.
	 */
	public final void addString(String value, String charsetName) {
		if (charsetName == null)
			charsetName = "UTF-8";
		try {
			addBytes(value.getBytes(charsetName));
			// jlog.info("addString: value="+value+
			// " position="+buffer.position());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(
					"Unexpected that UTF-8 is not available", e);
		}
	}

	public final void setString(String value) {
		clear();
		addString(value, null);
	}

	public final void clear() {
		buffer.clear();
	}

	/**
	 * Utility method for client-side implementation.
	 * 
	 * @return as bytes array
	 */
	public final byte[] getBytes() {
		// return buffer;
		byte[] a = new byte[buffer.position()];
		buffer.flip();
		buffer.get(a);
		buffer.limit(buffer.capacity());
		// jlog.info("getBytes: a.length="+a.length+
		// " position="+buffer.position()+ " limit="+buffer.limit());
		return a;
	}

	/**
	 * Utility method for client-side implementation.
	 * 
	 * @return the String value
	 */
	public final String toString() {
		try {
			byte[] a = getBytes();
			return new String(a, 0, a.length, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(
					"Unexpected that UTF-8 is not available", e);
		}
	}

	/**
	 * Returns the buffer to read or write to. The buffer is expected to be in
	 * writable before and will be so after been used. The call on this method
	 * should not lead to calculating the buffer.
	 * 
	 * @deprecated
	 * @return ByteBuffer representing the serialized or deserializable
	 *         Transferable.
	 */
	public ByteBuffer getBuffer() {
		return buffer;
	}

	/**
	 * The Transferable should serialize itself to buffer if the buffer is not
	 * up-to-date.
	 * 
	 * @deprecated
	 * @return ByteBuffer representing the serialized or deserializable
	 *         Transferable.
	 */
	public void doSerialize() {
		// always up-to-date by design
	}

	public void writeObject(ByteBuffer out) {
		buffer.flip();
		out.putShort((short) buffer.limit());
		out.put(buffer);
		buffer.limit(buffer.capacity());
	}

	public void readObject(ByteBuffer in) {
		buffer.clear();
		int size = in.getShort();
		int pos = in.position();
		int limit = in.limit();
		in.limit(pos + size);
		buffer.put(in);
		in.limit(limit);
	}

}