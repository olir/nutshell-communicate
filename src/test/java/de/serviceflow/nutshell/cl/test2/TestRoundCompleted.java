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
package de.serviceflow.nutshell.cl.test2;

import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.nio.Transfer;

/**
 * Example for a message be send from client to server. The class should be configured in
 * the XML protocol file, in this example <a href="testprotocol.xml"
 * target="_blank">testprotocol.xml</a>.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: $
 * 
 * 
 */
public class TestRoundCompleted extends Message<TestMessage2> {

	public TestRoundCompleted() {
		super(TestMessage2.TEST_ROUND_COMPLETED);
	}

	@Transfer
	public long muuid;
}
