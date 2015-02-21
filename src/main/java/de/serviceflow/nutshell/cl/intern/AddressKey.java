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
package de.serviceflow.nutshell.cl.intern;

import java.net.InetSocketAddress;

public class AddressKey implements Comparable<AddressKey> {
	private InetSocketAddress sa;

	AddressKey(InetSocketAddress sa) {
		this.sa = sa;
	}

	public int compareTo(AddressKey ak) {
		if (ak == null)
			return -1;
		if (sa == null)
			return 1;
		int c = sa.hashCode() - ak.hashCode();
		if (c == 0) {
			if (sa.equals(ak.getSocketAddress())) {
				return 0;
			}
			if (!sa.getAddress().equals(ak.getSocketAddress().getAddress())) {
				return -1;
			}
			return sa.getPort()-ak.getSocketAddress().getPort();
		}
		return c;
	}

	public InetSocketAddress getSocketAddress() {
		return sa;
	}

	public void setSocketAddress(InetSocketAddress sa) {
		this.sa = sa;
	}

	public String toString() {
		return "AddressKey#" + AddressKey.this.hashCode() + ": " + sa;
	}
}