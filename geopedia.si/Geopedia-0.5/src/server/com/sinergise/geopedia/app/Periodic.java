package com.sinergise.geopedia.app;

import it.unimi.dsi.fastutil.ints.Int2LongArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;

public abstract class Periodic
{
	static Int2LongArrayMap nextTimes = new Int2LongArrayMap();
	static IntHeapPriorityQueue queue = new IntHeapPriorityQueue(new IntComparator() {
		public int compare(int arg0, int arg1)
		{
			long t0 = nextTimes.get(arg0);
			long t1 = nextTimes.get(arg1);
			if (t0 < t1) return -1;
			if (t0 > t1) return 1;
			return 0;
		}
		
		public int compare(Integer o1, Integer o2)
		{
			return compare(o1.intValue(), o2.intValue());
		}
	});
	static Int2ReferenceArrayMap<TaskDef> tasks = new Int2ReferenceArrayMap<TaskDef>();

	private static int taskIdCounter = 1;
	
	public interface Task
	{
		/** returns whether it should be run again or not */
		public boolean execute();
	}

	public static final class TaskDef
	{
		private final int taskID;
		private final Task task;
		private final long skipTime;
		
		private TaskDef(int taskID, Task task, long skipTime)
		{
			this.taskID = taskID;
			this.task = task;
			this.skipTime = skipTime;
		}
	}

	public static TaskDef add(Task task, long skipTime)
	{
		if (task == null)
			return null;

		if (skipTime < 1)
			throw new IllegalArgumentException();

		synchronized (tasks) {
			int taskID = taskIdCounter++;
			TaskDef def = new TaskDef(taskID, task, skipTime);
			
			long nextRun = System.currentTimeMillis() + (long) (skipTime * Math.random());

			nextTimes.put(taskID, nextRun);
			tasks.put(taskID, def);
			queue.enqueue(taskID);
			
			checkThread();
			
			return def;
		}
	}

	static Executor executor = null;
	
	private static void checkThread()
	{
		if (executor == null || !executor.isAlive()) {
			executor = new Executor();
			executor.setDaemon(true);
			executor.setName("GPD: Periodic Executor");
			executor.start();
		}
	}
	
	private static class Executor extends Thread
	{
		public void run()
		{
			while (Main.isRunning()) {
				long currTime = System.currentTimeMillis();
				
				TaskDef task = null;
				long toWait = 0;
				
				synchronized(tasks) {
					if (tasks.size() < 1) {
						executor = null;
						return;
					}
					
					int nextTaskID = queue.firstInt();
					long nextTaskTime = nextTimes.get(nextTaskID);
					if (nextTaskTime > currTime) {
						toWait = nextTaskTime - currTime;
					} else {
						queue.dequeueInt();
						task = tasks.get(nextTaskID);
					}
				}
				
				if (task == null) {
					try {
	                    Thread.sleep(toWait);
                    } catch (InterruptedException e) {
                    }
                	continue;
				}
				
				boolean goAgain = true;
				try {
					goAgain = task.task.execute();
				} catch (Throwable t) {
					t.printStackTrace();
				}
				
				synchronized(tasks) {
					if (goAgain) {
						long nextTime = System.currentTimeMillis() + task.skipTime;
						nextTimes.put(task.taskID, nextTime);
						queue.enqueue(task.taskID);
					} else {
						nextTimes.remove(task.taskID);
						tasks.remove(task.taskID);
					}
				}
			}
		}
	}
}
