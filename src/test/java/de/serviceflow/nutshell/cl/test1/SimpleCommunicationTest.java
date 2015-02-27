/**
 * 
 */
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
package de.serviceflow.nutshell.cl.test1;

import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import de.serviceflow.nutshell.cl.NetworkProtocolType;
import de.serviceflow.nutshell.cl.ServerCommunication;
import de.serviceflow.nutshell.cl.test2.TestClient2;
import de.serviceflow.nutshell.cl.test2.TestServerDaemon2;

/**
 * A Simple Junit Test to check if client-server communication is working.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 5fa6fd8a4ea5e7a4ee22d54d8ffbd8d8b985384f $
 * 
 * 
 * @see TestClient2
 * @see TestServerDaemon2
 */
public class SimpleCommunicationTest {
	static final Logger JLOG = Logger.getLogger(SimpleCommunicationTest.class
			.getName());

	private static final long DELAY = 250L;

	public final int TESTPORT = 10101;
	private NetworkProtocolType npt = null;

	public SimpleCommunicationTest() {
		// LogManager.getLogManager().getLogger("").setLevel(Level.FINE);
	}

	/**
	 * <b>Test case</b> for TCP protocol.
	 *
	 * @see NetworkProtocolType#TCP
	 */
	@Test
	public final void testTCP() {
		npt = NetworkProtocolType.TCP;
		runProtocolTest();
	}

//	/**
//	 * <b>Test case</b> for UDP protocol.
//	 * 
//	 * @see NetworkProtocolType#UDP
//	 */
//	@Test
//	public void testUDP() {
//		npt = NetworkProtocolType.UDP;
//		runProtocolTest();
//	}

	/**
	 * Standard test procedure for any protocol.
	 */
	private void runProtocolTest() {
		try {

			// Setup a Server
			TestServerDaemon1 s = new TestServerDaemon1(this, null);
			ServerCommunication.getServerCommunication().setAuthentication(
					new TestAuthentication());
			s.startDaemon();

			// Setup a Client
			TestClient1 c = new TestClient1(this);
			c.connect();

			// Test communication: Send TestRequst, Evaluate TEST_ACKNOWLEDGE.

			try {
				Thread.sleep(DELAY);

				if (c.getTestData().getClientCalculatedProduct() != c
						.getTestData().getServerCalculatedProduct()) {
					fail("Result of reponse is wrong. Expected "
							+ c.getTestData().getClientCalculatedProduct()
							+ ", got: "
							+ c.getTestData().getServerCalculatedProduct());
				}
				JLOG.info("Success: APPLICATION exchange correct.");
			} finally {
				// Stop communication threads.
				c.stop();
				s.stop();
			}
			Thread.sleep(DELAY);
		} catch (Throwable t) {
			JLOG.log(Level.SEVERE, "Error Stack:", t);
			fail("Error occured: " + t);
		}
	}

	public Reader createProtocolReader() {
		InputStream is = getClass().getResourceAsStream(
				"/de/serviceflow/nutshell/cl/test/testprotocol1.xml");
		if (is == null) {
			fail("protocol resource not found");
		}
		return new InputStreamReader(is);
	}

	public final NetworkProtocolType getNpt() {
		return npt;
	}

}
