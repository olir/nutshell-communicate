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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.intern.MessageRegistryHelper;
import de.serviceflow.nutshell.cl.intern.NioSession;
import de.serviceflow.nutshell.cl.intern.namp.MessageClassification;
import de.serviceflow.nutshell.cl.intern.util.Bucket;
import de.serviceflow.nutshell.cl.intern.util.Pool;
import de.serviceflow.nutshell.cl.nio.NioStruct;

/**
 * Base class for application messages.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: 084ef77aa8b2b76d509025f57bccbf0aa03410ab $
 * 
 * 
 */
public abstract class Message<EnumClass> extends NioStruct {
	private static final Logger jlog = Logger
			.getLogger(Message.class.getName());

	protected boolean bufferGetMode;

	private int commandId;
	private int classificationValue;
	private Enum<?> command = null;

	private NioSession session;

	@SuppressWarnings("unchecked")
	private static Map<Class<?>, MPool<Message<?>>>[] poolMapArray = new Map[ApplicationProtocol.MAX_PROTOCOLS];
	@SuppressWarnings("unchecked")
	private static List<Class<?>>[] poolListArray = new List[ApplicationProtocol.MAX_PROTOCOLS];

	/**
	 * @see MessageClassification
	 */
	public static Message<?> requestMessage(Class<?> c, Session session) {
		return requestMessage(c, session.getApplicationProtocol());
	}

	/**
	 * @see MessageClassification
	 */
	public static Message<?> requestMessage(Class<?> c, ApplicationProtocol p) {
		return requestMessage(c, p.getId());
	}

	/**
	 * @see MessageClassification
	 */
	public static Message<?> requestMessage(int commandId, int controlType) {
		if (controlType == -1) {
			throw new Error("APPLICATION class not registered for commandId="
					+ commandId + "in #" + controlType);
		}
		if (poolListArray[controlType] == null) {
			poolListArray[controlType] = new ArrayList<Class<?>>();
		}
		if (commandId >= poolListArray[controlType].size()) {
			throw new Error("APPLICATION class not registered for commandId="
					+ commandId + "in #" + controlType);
		}
		Class<?> c = poolListArray[controlType].get(commandId);
		if (c == null) {
			throw new Error("APPLICATION class not registered for commandId="
					+ commandId + "in #" + controlType);
		}
		return requestMessage(c, controlType);
	}

	/**
	 * @see MessageClassification
	 */
	public static Message<?> requestMessage(Class<?> c, int controlType) {
		if (poolMapArray[controlType] == null) {
			poolMapArray[controlType] = new HashMap<Class<?>, MPool<Message<?>>>();
		}
		Pool<Message<?>> pool = poolMapArray[controlType].get(c);
		if (pool == null) {
			throw new Error("APPLICATION class not registered for class"
					+ c.getName() + "in message class #" + controlType
					+ ". Please check if your protocol xml is valid.");
		}
		Message<?> m = pool.requestElementFromPool();
		m.setClassificationValue(controlType); 
		return m;
	}

	public String toString() {
		return getClass().getSimpleName() + "[" + commandId + "("
				+ classificationValue + ")]";
	}

	/**
	 * clean up and put message into pool.
	 */
	public void releaseMessage() {
		if (getClassificationValue() == -1)
			return; // ignore prototypes

		// pool it
		Map<Class<?>, MPool<Message<?>>> map = poolMapArray[getClassificationValue()];
		if (map != null) {
			Pool<Message<?>> pool = map.get(getClass());
			if (pool != null) {
				pool.releaseElementToPool((Message<?>) this);
			}
		}
	}

	private MessageRegistryHelper messageDefinition = MessageRegistryHelper.DEFAULT;

	/**
	 * Creates an application message.
	 * 
	 * @param command
	 *            The application message enumerator.
	 */
	protected Message(Enum<?> command) {
		this(command, -1);
	}

	// /**
	// * Creates a transport control message.
	// * <p>
	// * Do not use outside library.
	// *
	// * @param id
	// * control message id
	// * @param type
	// * MessageClassification.
	// */
	// protected Message(int id, int classificationValue) {
	// this.command = null;
	// this.commandId = id;
	// this.classificationValue = classificationValue;
	// }

	public final Enum<?> getCommand() {
		return command;
	}

	/**
	 * Creates a transport control message.
	 * <p>
	 * Do not use outside library.
	 * 
	 * @param command
	 *            The classification-specific message enumerator.
	 * @param type
	 *            MessageClassification.
	 */
	protected Message(Enum<?> command, int classificationValue) {
		updateCommand(command, classificationValue);
	}

	protected void updateCommand(Enum<?> command, int classificationValue) {
		this.command = command;
		if (command != null) {
			this.commandId = command.ordinal();
		} else {
			this.commandId = -1;
		}
		this.classificationValue = classificationValue;
	}

	private void setMessageDefinition(MessageRegistryHelper messageDefinition) {
		this.messageDefinition = messageDefinition;
	}

	public MessageRegistryHelper getMessageDefinition() {
		return messageDefinition;
	}

	public final int getCommandId() {
		return commandId;
	}

	public final int getClassificationValue() {
		return classificationValue;
	}

	/**
	 * for internal registration during ApplicationProtocol build.
	 * 
	 * @param messageClass
	 * @param controlType
	 * @return
	 * @throws IllegalAccessException
	 */
	public static MessageRegistryHelper register(final Class<?> messageClass,
			int controlType) throws IllegalAccessException {
		if (jlog.isLoggable(Level.FINER)) {
			jlog.finer("AbstractMessage.register(): " + messageClass.getName());
		}
		Map<Class<?>, MPool<Message<?>>> map = poolMapArray[controlType];
		if (map == null) {
			map = new HashMap<Class<?>, MPool<Message<?>>>();
			poolMapArray[controlType] = map;
		}
		List<Class<?>> list = poolListArray[controlType];
		if (list == null) {
			list = new ArrayList<Class<?>>();
			poolListArray[controlType] = list;
		}

		MPool<Message<?>> pool = map.get(messageClass);
		if (pool == null) {// Not registered yet
			pool = new MPool<Message<?>>(100) {
				protected Message<?> newInstance() {
					try {
						Message<?> m = (Message<?>) messageClass.newInstance();
						m.setMessageDefinition(getMessageDefinition());
						return m;
					} catch (InstantiationException e) {
						e.printStackTrace();
						throw new Error("InstanciationError", e);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						throw new Error("InstanciationError", e);
					}
				}
			};
			pool.setMessageDefinition(new MessageRegistryHelper());
			poolMapArray[controlType].put(messageClass, pool);

			Message<?> prototype = Message.requestMessage(messageClass,
					controlType);
			prototype.setClassificationValue(controlType);
			int id = prototype.getCommandId();
			for (int i = poolListArray[controlType].size(); i < id; i++) {
				poolListArray[controlType].add(null);
			}
			if (poolListArray[controlType].size() == id) {
				poolListArray[controlType].add(messageClass);
			} else {
				poolListArray[controlType].set(id, messageClass);
			}

			prototype.releaseMessage();
		}
		return pool.getMessageDefinition();
	}

	public NioSession getSession() {
		return session;
	}

	public void setSession(NioSession session) {
		this.session = session;
	}

	private static abstract class MPool<T> extends Bucket<T> {
		public MPool(int capacity) {
			super(capacity);
		}

		private MessageRegistryHelper def = null;

		public MessageRegistryHelper getMessageDefinition() {
			return def;
		}

		public void setMessageDefinition(MessageRegistryHelper def) {
			this.def = def;
		}

		@Override
		protected void toss(T e) {
			// TODO Auto-generated method stub
			
		}
	}

	public static int pipesize(Class<?> c, Session s) {
		return pipesize(c, s.getApplicationProtocol().getId());
	}

	private static int pipesize(Class<?> c, int controlType) {
		if (poolMapArray[controlType] == null) {
			poolMapArray[controlType] = new HashMap<Class<?>, MPool<Message<?>>>();
		}
		Pool<Message<?>> pool = poolMapArray[controlType].get(c);
		return pool.size();
	}

	public void setClassificationValue(int classificationValue) {
		this.classificationValue = classificationValue;
	}
}
