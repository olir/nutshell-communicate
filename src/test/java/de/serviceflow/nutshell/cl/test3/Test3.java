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
package de.serviceflow.nutshell.cl.test3;

import static org.junit.Assert.fail;

import java.util.logging.Logger;

/**
 * Another Test to check if client-server communication is working.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 5fa6fd8a4ea5e7a4ee22d54d8ffbd8d8b985384f $
 */
public class Test3 {
	static final Logger JLOG = Logger.getLogger(Test3.class.getName());

	public void doTest3() {
		try {
			TestServer3 server = new TestServer3();
			server.bind();

			TestClient3 client = new TestClient3();
			client.connect();

			Thread.sleep(500L);
			client.stop();
			server.stop();
		} catch (Throwable e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}
}
