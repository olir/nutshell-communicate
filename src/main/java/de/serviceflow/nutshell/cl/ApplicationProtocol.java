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

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import de.serviceflow.nutshell.cl.intern.SessionObject;
import de.serviceflow.nutshell.cl.xml.Protocol;

/**
 * Class to load the applications's protocol.
 * <p>
 * An instance has to be initialized by access ({@link Reader}) to a XML
 * definition of the protocol that satisfies the schema <a
 * href="../../../../schema/protocol.xsd" target="_blank">protocol.xsd</a>.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 25b6e394ec1d12247bcc1351273bc342ff46e741 $
 * 
 * 
 */
public final class ApplicationProtocol {
	private static final Logger jlog = Logger
			.getLogger(ApplicationProtocol.class.getName());

	public static final int MAX_PROTOCOLS = 255;
	
	private static final String CONTEXT_PATH = "de.serviceflow.nutshell.cl.xml";

	private final int id;
	
	private APState state[];
	private APState initialState = null;

	private final String messageClassPackage;
	private final String name;

	private static Map<String, ApplicationProtocol> instanceMap = new HashMap<String, ApplicationProtocol>();
	private static int protocolCount = 0;
	
	@SuppressWarnings("unchecked")
	public static ApplicationProtocol createInstance(Reader xmlProtocolReader)
			throws JAXBException, ClassNotFoundException,
			IllegalAccessException {
		// if (instance!=null)
		// return instance;

		JAXBContext context = JAXBContext.newInstance(CONTEXT_PATH);
		Unmarshaller unmarshaller = context.createUnmarshaller();

		JAXBElement<Protocol> jaxbElement = (JAXBElement<Protocol>) unmarshaller
				.unmarshal(xmlProtocolReader);

		Protocol xProtocol = jaxbElement.getValue();
		String key = xProtocol.getName();
		ApplicationProtocol instance = instanceMap.get(key);
		if (instance == null) {
			if (protocolCount==MAX_PROTOCOLS)
				throw new Error("Maximum number of protocols exceeded (255)");
			protocolCount++; // register instance and messsages under this
			instance = new ApplicationProtocol(jaxbElement);
			instanceMap.put(key, instance);
			if (jlog.isLoggable(SessionObject.MSG_TRACE_LEVEL)) {
				jlog.log(SessionObject.MSG_TRACE_LEVEL, "Added protocol '" + key + "'");
			}
		}
		return instance;
	}

	private ApplicationProtocol(JAXBElement<Protocol> jaxbElement)
			throws JAXBException, ClassNotFoundException,
			IllegalAccessException {

		id = protocolCount;
		Protocol xProtocol = jaxbElement.getValue();
		name = xProtocol.getName();
		messageClassPackage = xProtocol.getMessagepackage();
		int i = 0;
		List<de.serviceflow.nutshell.cl.xml.State> xStates = xProtocol
				.getState();
		state = new APState[xStates.size()];
		for (de.serviceflow.nutshell.cl.xml.State xState : xStates) {
			state[i++] = new APState(xState, this);
			if (xState.isIsInitial()) {
				if (initialState != null) {
					throw new Error(
							"Protocol error. Exactly one initial state expected, but multiple initial states have been defined. Check your protocol XML file.");
				}
				initialState = state[i - 1];
			}
		}
		if (initialState == null) {
			throw new Error(
					"Protocol error. Exactly one initial state expected, but no initial state was defined. Check your protocol XML file.");
		}
	}

	public APState getState(String name) {
		for (APState s : state) {
			if (name.equals(s.getName())) {
				return s;
			}
		}
		return null;
	}

	public final String getMessageClassPackage() {
		return messageClassPackage;
	}

	public APState getInitialState() {
		return initialState;
	}

	public void setInitialState(APState initialState) {
		this.initialState = initialState;
	}

	public static ApplicationProtocol getByName(String name) {
		return instanceMap.get(name);
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public static int getProtocolCount() {
		return protocolCount;
	}

}
