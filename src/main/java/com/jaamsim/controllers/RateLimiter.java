/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2014 Ausenco Engineering Canada Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package com.jaamsim.controllers;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A class to limit updates to a set rate in real time
 * @author matt.chudleigh
 *
 */
public class RateLimiter {
	private final Timer timer;
	private final RateLimitedTimerTask task;

	private long lastTime = 0;
	private long scheduledTime = 0;
	private final Object timingLock = new Object();
	private final double ups;

	public RateLimiter(double updatesPerSecond) {
		// Start the display timer
		ups = updatesPerSecond;
		timer = new Timer("UpdateThread", true);
		task = new RateLimitedTimerTask();
		timer.scheduleAtFixedRate(task, 0, (long) (1000 / (ups*2)));
	}

	private class RateLimitedTimerTask extends TimerTask {
		final ArrayList<Runnable> callbacks = new ArrayList<Runnable>();

		@Override
		public void run() {
			synchronized(timingLock) {
				// Is a redraw scheduled
				long currentTime = System.currentTimeMillis();

				// Only update if the scheduled time is before now and after the last update
				if ((scheduledTime < lastTime || currentTime < scheduledTime)) {
					return;
				}

				lastTime = currentTime;
			}

			synchronized (callbacks) {
				for (Runnable r : callbacks) {
					r.run();
				}
			}
		}
	}

	public void queueUpdate() {
		synchronized(timingLock) {
			if (scheduledTime > lastTime) {
				// A draw is scheduled
				return;
			}

			long currentTime = System.currentTimeMillis();
			long newDraw = currentTime;
			long frameTime = (long)(1000.0/ups);
			if (newDraw < lastTime + frameTime) {
				// This would be scheduled too soon
				newDraw = lastTime + frameTime;
			}
			scheduledTime = newDraw;
		}
	}

	public void registerCallback(Runnable r) {
		synchronized (task.callbacks) {
			task.callbacks.add(r);
		}
	}

	public void cancel() {
		timer.cancel();
	}
}