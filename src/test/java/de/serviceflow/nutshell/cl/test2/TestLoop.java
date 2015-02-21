package de.serviceflow.nutshell.cl.test2;

import java.util.UUID;
import java.util.logging.Logger;

import de.serviceflow.nutshell.cl.Message;
import de.serviceflow.nutshell.cl.Session;

public class TestLoop implements Runnable {
	private static final Logger JLOG = Logger.getLogger(TestLoop.class
			.getName());

	private final int amount;
	private final long sleepTime;
	private final int msgPerSleep;
	private final long INITIAL_SLEEP = 1000L;
	private final long FINAL_SLEEP;
	private final Session session;
	private boolean sendMode = true;
	private long t0;

	public TestLoop(Session session, int amount, long sleepTime, int msgPerSleep) {
		this.session = session;
		this.amount = amount;
		this.sleepTime = sleepTime;
		this.msgPerSleep = msgPerSleep;

		FINAL_SLEEP = 1000L + amount / msgPerSleep * sleepTime;
	}

	@Override
	public final void run() {
		long u;
		while (true) {
			if (sendMode) {
				u = UUID.randomUUID().getMostSignificantBits();

				TestRoundStarting m1 = (TestRoundStarting) Message
						.requestMessage(TestRoundStarting.class, session);
				m1.muuid = u;
				m1.amount = amount;
				session.send(m1);

				try {
					Thread.sleep(INITIAL_SLEEP);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				t0 = System.currentTimeMillis();

				for (int i = 0; i < amount; i++) {
					TestPing m2 = (TestPing) Message
							.requestMessage(TestPing.class, session);
					m2.muuid = u;
					m2.id = i;
					session.send(m2);
					if (sleepTime > 0 && msgPerSleep > 0) {
						if (i % msgPerSleep == 0) {
							try {
								Thread.sleep(sleepTime);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
				try {
					Thread.sleep(FINAL_SLEEP);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				TestRoundCompleted m3 = (TestRoundCompleted) Message
						.requestMessage(TestRoundCompleted.class, session);
				m3.muuid = u;
				session.send(m3);
				sendMode = false;
			} else {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	long tlost = 0;
	long iterations = 0;

	public final void stopTimer(TestAcknowledge m, Session s) {
		long t1 = System.currentTimeMillis();
		long travelTime = t1 - t0 - FINAL_SLEEP - m.oldwait;
		tlost += (amount - m.count);
		iterations++;
		long l_loss = 100 * tlost / iterations / amount;
		float loss = l_loss;
		if (l_loss == 0L) {
			loss = 100.0f * tlost / iterations / amount;
		}
		JLOG.info("" + amount + " msg traveled in " + travelTime
				+ "ms while lost " + (amount - m.count) + ". pipesize="
				+ Message.pipesize(TestPing.class, s) + ". Ignored " + m.old
				+ " old and " + m.copies + " copies. Total loss " + loss + "%.");
		sendMode = true;
	}

}
