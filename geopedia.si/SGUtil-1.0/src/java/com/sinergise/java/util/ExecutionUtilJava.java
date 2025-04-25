package com.sinergise.java.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.sinergise.common.util.ExecutionUtil;
import com.sinergise.common.util.lang.SGCallable;

public class ExecutionUtilJava extends ExecutionUtil {
	
	private static class BlockingExecutionService extends AbstractExecutorService {
		private final ExecutorService delegate;
		private final LinkedBlockingQueue<Runnable> waiting; 
		private final LinkedBlockingQueue<Runnable> executing; 
		
		public BlockingExecutionService(int maxActiveThreads) {
			delegate = Executors.newCachedThreadPool();
			waiting = new LinkedBlockingQueue<Runnable>();
			executing = new LinkedBlockingQueue<Runnable>(maxActiveThreads);
		}
		
		@Override
		public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
			return delegate.awaitTermination(timeout, unit);
		}
		
		@Override
		public void execute(final Runnable command) {
			synchronized (this) {
				if (delegate.isShutdown()) {
					throw new RejectedExecutionException("Service already shut down");
				}
			}
			waitForExecution(command);
			internalExecute(command);
		}

		private void internalExecute(final Runnable command) {
			if (!waiting.remove(command)) {
				throw new RejectedExecutionException("Execution rejected. Queue probably shut down.");
			}
			delegate.execute(new Runnable() {
				@Override
				public void run() {
					try {
						command.run();
					} finally {
						commandCompleted(command);
					}
				}
			});
		}

		private void waitForExecution(final Runnable command) {
			try {
				waiting.put(command);
				executing.put(command);
			} catch(InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		

		void commandCompleted(final Runnable command) {
			executing.remove(command);
		}
		
		@Override
		public synchronized boolean isShutdown() {
			return delegate.isShutdown();
		}
		
		@Override
		public boolean isTerminated() {
			return delegate.isTerminated();
		}
		
		@Override
		public synchronized void shutdown() {
			while (executing.drainTo(new ArrayList<Runnable>()) > 0) {
				Thread.yield();
			}
			while (!waiting.isEmpty()) {
				Thread.yield();
			}
			delegate.shutdown();
		}
		
		@Override
		public synchronized List<Runnable> shutdownNow() {
			ArrayList<Runnable> ret = new ArrayList<Runnable>();
			waiting.drainTo(ret);
			ret.addAll(delegate.shutdownNow());
			return ret;
		}
	}
	
	
	public static final <T, P> T executeWithOutOfMemoryRetry(final int nTries, final long delay, final SGCallable<T> toRun) throws OutOfMemoryError {
		return MemoryUtil.executeWithOutOfMemoryRetry(nTries, delay, toRun);
	}
	
	public static boolean sleep(long millis, int nanos) {
		if (millis < 0) {
			millis = 0;
		}
		if (nanos < 0) {
			nanos = 0;
		}
		try {
			if (millis > 0 || nanos > 0) {
				Thread.sleep(millis, nanos);
			}
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}

	public static boolean executeWithRetry(final SGCallable<Boolean> toRun, int nTries, int sleepMs) {
		int tryCnt = 0;
		while (tryCnt++ < nTries) {
			if (toRun.call().booleanValue()) {
				return true;
			}
			if (!sleep(sleepMs, 0)) {
				return false;
			}
		}
		return false;
	}

	public static ExecutorService newBlockingThreadPool(final int nThreads) {
		return new BlockingExecutionService(nThreads);
	}
	
	public static void shutdownAndAwaitTermination(ExecutorService service, long timeout, TimeUnit unit) {
		service.shutdown();
		try {
			service.awaitTermination(timeout, unit);
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
