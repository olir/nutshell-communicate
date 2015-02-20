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

/**
 * Enumeration of supported protocol implementations.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: e5986aa057846c19d39fbe5313c7fe48b66bb0c5 $
 * 
 * 
 */
public enum NetworkProtocolType {
	// Putted some stuff from wikipedia in here :-)

	/**
	 * TCP is a reliable stream delivery service that guarantees that all bytes
	 * received will be identical with bytes sent and in the correct order.
	 * <p>
	 * The Transmission Control Protocol (TCP) is one of the core protocols of
	 * the Internet protocol suite (IP), and is so common that the entire suite
	 * is often called TCP/IP. TCP provides reliable, ordered and error-checked
	 * delivery of a stream of octets between programs running on computers
	 * connected to a local area network, intranet or the public Internet. It
	 * resides at the transport layer.
	 */
	TCP,

	/**
	 * UDP is a minimal message-oriented Transport Layer protocol. It is
	 * stateless, suitable for very large numbers of clients. The protocol
	 * provides no error checking and delivery validation, which need to be done
	 * by the library if configured in the message (protocol).
	 * <p>
	 * The Principal Datagram Protocol (UDP) is one of the core members of the
	 * Internet protocol suite. The protocol was designed by David P. Reed in
	 * 1980 and formally defined in RFC 768.
	 * <p>
	 * UDP uses a simple connectionless transmission model with a minimum of
	 * protocol mechanism. It has no handshaking dialogues, and thus exposes any
	 * unreliability of the underlying network protocol to the user's program.
	 * There is no guarantee of delivery, ordering, or duplicate protection. UDP
	 * provides checksums for data integrity, and port numbers for addressing
	 * different functions at the source and destination of the datagram.
	 * <p>
	 * With UDP, computer applications can send messages, in this case referred
	 * to as datagrams, to other hosts on an Internet Protocol (IP) network
	 * without prior communications to set up special transmission channels or
	 * data paths. UDP is suitable for purposes where error checking and
	 * correction is either not necessary or is performed in the application,
	 * avoiding the overhead of such processing at the network interface level.
	 * Time-sensitive applications often use UDP because dropping packets is
	 * preferable to waiting for delayed packets, which may not be an option in
	 * a real-time system.
	 */
	UDP,

	/**
	 * Dual-channel: TCP and UDP. UDP is used for messages that are flagged as
	 * not reliable.
	 */
	TCP_UDP
}
