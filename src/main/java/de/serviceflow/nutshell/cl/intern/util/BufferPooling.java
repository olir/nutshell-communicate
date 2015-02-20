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

import java.nio.ByteBuffer;

/**
 * Pool for ByteBuffer.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: fbcfd2e7a099d4417e57756090446d0f9f3e5ef0 $
 * 
 * 
 */
public class BufferPooling {

	private static final int BUCKET_INDEX_OF_BIGGESTBUFFER = 16;

	private static final int BUCKET_INDEX_OF_LIMITED_CAP = 14;
	private static final int LIMITED_CAP = 4;
	private static final int STANDARD_CAP = 16;

	@SuppressWarnings("unchecked")
	private final Bucket<ByteBuffer>[] bufferBuckets = new Bucket[BUCKET_INDEX_OF_BIGGESTBUFFER];

	private long tossedBytes = 0;
	private long allocatedBytes = 0;

	public BufferPooling() {
		for (int i = 0; i < BUCKET_INDEX_OF_LIMITED_CAP; i++)
			bufferBuckets[i] = new ByteBufferBucket(STANDARD_CAP);
		for (int i = BUCKET_INDEX_OF_LIMITED_CAP; i < BUCKET_INDEX_OF_BIGGESTBUFFER; i++)
			bufferBuckets[i] = new ByteBufferBucket(LIMITED_CAP);
	}

	public void releaseByteBuffer(ByteBuffer b) {
		int bucketIndex = log2(b.capacity());
		if (bucketIndex >= BUCKET_INDEX_OF_BIGGESTBUFFER) {
			toss(b);
			return;
		} else if (bucketIndex > BUCKET_INDEX_OF_LIMITED_CAP) {
			push(b, bucketIndex, LIMITED_CAP);
		} else {
			push(b, bucketIndex, STANDARD_CAP);
		}
	}

	public ByteBuffer getByteBuffer(int capacity) {
		int bucketIndex = log2(capacity);
		if (bucketIndex >= BUCKET_INDEX_OF_BIGGESTBUFFER) {
			allocatedBytes += capacity;
			return ByteBuffer.allocateDirect(capacity);
		} else {
			ByteBuffer b = bufferBuckets[bucketIndex].requestElementFromPool();
			b.clear();
			return b;
		}
	}

	public ByteBuffer upgradeByteBuffer(ByteBuffer src, int requiredCapacity) {
		int c = src.capacity();
		if (c >= requiredCapacity) {
			return src;
		}

		ByteBuffer newBuffer = getByteBuffer(requiredCapacity);
		src.flip();
		newBuffer.put(src);
		releaseByteBuffer(src);
		return newBuffer;
	}

	private void push(ByteBuffer b, int bucketIndex, int tossThreshold) {
		bufferBuckets[bucketIndex].releaseElementToPool(b);
	}

	protected void toss(ByteBuffer b) {
		tossedBytes += b.capacity();
	}

	private static int log2(int n) {
		return 31 - Integer.numberOfLeadingZeros(n);
	}

	public long getTossedBytes() {
		return tossedBytes;
	}

	public long getAllocatedBytes() {
		return allocatedBytes;
	}

	private class ByteBufferBucket extends Bucket<ByteBuffer> {
		public ByteBufferBucket(int capacity) {
			super(capacity);
		}

		@Override
		protected void toss(ByteBuffer b) {
			BufferPooling.this.toss(b);
		}

		@Override
		protected ByteBuffer newInstance() {
			allocatedBytes += getCapacity();
			return ByteBuffer.allocateDirect(getCapacity());
		}
	}
}
