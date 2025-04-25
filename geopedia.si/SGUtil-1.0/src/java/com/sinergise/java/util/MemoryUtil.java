package com.sinergise.java.util;

import com.sinergise.common.util.lang.SGCallable;

public class MemoryUtil {

	public static final <T, P> T executeWithOutOfMemoryRetry(final int nTries, final long delay, final SGCallable<T> toRun) throws OutOfMemoryError {
		int failCount = 0;
		OutOfMemoryError origE = null;
		while (true) {
			try {
				return toRun.call();
			} catch(final OutOfMemoryError e) {
				if (origE == null) {
					origE = e;
				}
				failCount++;
				if (failCount >= nTries) {
					final OutOfMemoryError oome = new OutOfMemoryError("Tried " + failCount + " times and still not enough memory...");
					oome.initCause(e);
					throw oome;
				}
				try {
					Runtime.getRuntime().gc();
					// Wait a bit for the GC to do its job
					// (it was supposed to have done it by the time gc() returns, but one never knows)
					// or for other threads to free some memory
					Thread.sleep(delay);
					// Run GC again in case we've freed some memory in the meantime
					Runtime.getRuntime().gc();
				} catch(final Exception ex) {}
			}
		}
	}
}
