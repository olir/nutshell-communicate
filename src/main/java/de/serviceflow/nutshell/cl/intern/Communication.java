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

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.xml.bind.JAXBException;

import de.serviceflow.nutshell.cl.ApplicationProtocol;
import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.MessageListener;
import de.serviceflow.nutshell.cl.SessionListener;
import de.serviceflow.nutshell.cl.intern.session.ChangeState;
import de.serviceflow.nutshell.cl.intern.session.ClientAuthentication;
import de.serviceflow.nutshell.cl.intern.session.MessageClassification;
import de.serviceflow.nutshell.cl.intern.session.SessionAccepted;
import de.serviceflow.nutshell.cl.intern.session.SessionClosed;
import de.serviceflow.nutshell.cl.intern.session.StateChangeAcknowledged;

/**
 * Base class for communication that separates application and transport layer.
 * 
 * @author <a href="http://www.serviceflow.de/nutshell">Oliver Rode</a>
 * @version $Id: f4e587999dfbba947b485144f6a43e04f3439422 $
 * 
 * 
 */
public abstract class Communication {
	static private final Logger JLOG = Logger.getLogger(Communication.class
			.getName());

	public static final String MBEAN_PACKAGE = "de.serviceflow.nutshell.cl";

	private static MBeanServer mbeanServer = null;

	protected final SessionListenerDispensor eventHelper = new SessionListenerDispensor(
			this);
	private final List<MessageListener> mlisteners = new ArrayList<MessageListener>();
	private final List<SessionListener> slisteners = new ArrayList<SessionListener>();

	private ThreadGroup group = new ThreadGroup("Nutshell Communication Library");

	

	protected final List<Runnable> communicationWorkers = Collections
			.synchronizedList(new ArrayList<Runnable>());

	protected Communication() {
		try {
			Message.register(ClientAuthentication.class,
					MessageClassification.SESSION.value());
			Message.register(SessionAccepted.class,
					MessageClassification.SESSION.value());
			Message.register(SessionClosed.class,
					MessageClassification.SESSION.value());
			Message.register(ChangeState.class,
					MessageClassification.SESSION.value());
			Message.register(StateChangeAcknowledged.class,
					MessageClassification.SESSION.value());
		} catch (IllegalAccessException e) {
			throw new Error(
					"Unrecoverable internal missconfiguration. Bugfix required.");
		}		
	}

	public final void addApplicationProtocol(Reader r) {
		try {
			ApplicationProtocol.createInstance(r); // ensure it is created
		} catch (ClassNotFoundException | IllegalAccessException
				| JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public final void addSessionListener(SessionListener l) {
		slisteners.add(l);
		if (JLOG.isLoggable(Level.FINEST)) {
			JLOG.finest("SessionListener installed");
		}
	}

	public final void removeSessionListener(SessionListener l) {
		slisteners.remove(l);
	}

	public final void addMessageListener(MessageListener l) {
		mlisteners.add(l);
		if (JLOG.isLoggable(Level.FINEST)) {
			JLOG.finest("MessageListener installed");
		}
	}

	public final void removeMessageListener(MessageListener l) {
		mlisteners.remove(l);
	}

	public final void removeAllListeners() {
		slisteners.clear();
		mlisteners.clear();
	}

	public final SessionListenerDispensor getProtocolListenerHelper() {
		return eventHelper;
	}

	public final List<SessionListener> getSlisteners() {
		return slisteners;
	}

	public final List<MessageListener> getMlisteners() {
		return mlisteners;
	}

	public final ThreadGroup getThreadGroup() {
		return group;
	}

	public static MBeanServer getMbeanServer() {
		return mbeanServer;
	}

	public static void setMbeanServer(MBeanServer mbs) {
		mbeanServer = mbs;
	}

	/**
	 * Add a Runnable that is high-frequently called. The run-method is expected
	 * to run fast. All communication holds while it executes.
	 * 
	 * @param r
	 *            the Runnable
	 * @return true
	 */
	public final boolean addCommunicationWorker(Runnable r) {
		return communicationWorkers.add(r);
	}

	public final boolean removeCommunicationWorker(Runnable r) {
		return communicationWorkers.remove(r);
	}

	protected boolean inCommunicationThread = false;

	public boolean isInCommunicationThread() {
		return inCommunicationThread;
	}

	protected void setInCommunicationThread(boolean inCommunicationThread) {
		this.inCommunicationThread = inCommunicationThread;
	}

	protected abstract void communicationStep();

	protected class CommunicationLoop implements Runnable {

		public CommunicationLoop() {
		}

		boolean running = true;

		public void run() {
			for (; running;) {
				try {
					Communication.this.communicationStep();
				} catch (Throwable t) {
					JLOG.log(Level.SEVERE, "Exception in communication Loop", t);
				}
				sleep();
			}
			JLOG.log(Level.INFO, "*** Finished communication thread.");
		}

		private void sleep() {
			try {
				Thread.sleep(1L);
			} catch (InterruptedException e) {
				running = false;
				JLOG.log(Level.SEVERE, "Exception in communication Loop", e);
			}
		}

	}

}
