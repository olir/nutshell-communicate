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

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import de.serviceflow.nutshell.cl.intern.NioSession;
import de.serviceflow.nutshell.cl.intern.SessionObject;
import de.serviceflow.nutshell.cl.intern.util.Bucket;

/**
 * Proxy for a APPLICATION to be send to multiple targets. Can't be used on
 * client-side.
 * <p>
 * Request an instance as proxy to for the real message with
 * {@link MultiTargetMessage#requestMultiTargetMessage(APPLICATION)}, then
 * assign targets by {@link #addTarget(SessionObject)} before putting it
 * (instead of real message= into the send queue.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 084ef77aa8b2b76d509025f57bccbf0aa03410ab $
 * 
 * 
 */
public class MultiTargetMessage<EnumClass> extends Message<EnumClass> {
	private static final int BUFFER_SIZE = 1024;

	private ByteBuffer reusedWriteBuffer = ByteBuffer
			.allocateDirect(BUFFER_SIZE);

	private static final Bucket<MultiTargetMessage<?>> MMPOOL = new Bucket<MultiTargetMessage<?>>(
			100) {
		@SuppressWarnings("rawtypes")
		protected MultiTargetMessage<?> newInstance() {
			return new MultiTargetMessage();
		}

		@Override
		protected void toss(MultiTargetMessage<?> e) {
			// TODO Auto-generated method stub

		}

	};;

	private final Set<SessionObject> targets = new HashSet<SessionObject>();

	private Message<?> real;
	private int targetCount = 0;
	@SuppressWarnings("unused")
	private boolean reusedWriteBufferInitialized;

	public static MultiTargetMessage<?> requestMultiTargetMessage(
			Message<?> real) {
		MultiTargetMessage<?> mtm = MMPOOL.requestElementFromPool();
		mtm.assignMessage(real);
		return mtm;
	}

	public void addTarget(SessionObject s) {
		targets.add(s);
		targetCount++;
	}

	private MultiTargetMessage() {
		super(null, -1);
	}

	private void assignMessage(Message<?> real) {
		this.real = real;
		updateCommand(real.getCommand(), real.getClassificationValue());
		super.bufferGetMode = true;
		// super.broadcastMode = true;
		if (real.bufferGetMode != true) {
			throw new Error("Buffer not in get mode: " + real);
		}
		reusedWriteBuffer.clear();
		reusedWriteBufferInitialized = false;
	}

	public void releaseMessage() {
		targetCount--;
		if (targetCount == 0) {
			real.releaseMessage();
			super.releaseMessage();
		} else if (targetCount < 0) {
			throw new Error("Released message to often: " + real);
		}
	}

	// TODO rewrite for Composite
	// public ByteBuffer deserializeFromBuffer(ByteBuffer b) {
	// throw new Error("not available for broadcasts.");
	// }
	//
	// /**
	// * Called multiple times, for every target ...
	// *
	// * @param b
	// * ByteBuffer of the current session.
	// */
	// public ByteBuffer serializeToBuffer(ByteBuffer b) {
	// if (!reusedWriteBufferInitialized) {
	// real.serializeToBuffer(reusedWriteBuffer);
	// reusedWriteBufferInitialized = true;
	// }
	// reusedWriteBuffer.mark();
	// b.put(reusedWriteBuffer);
	// reusedWriteBuffer.rewind();
	// return b;
	// }

	@Override
	public void setSession(NioSession session) {
		throw new Error("broadcasts should not be assigned a session");
	}

}
