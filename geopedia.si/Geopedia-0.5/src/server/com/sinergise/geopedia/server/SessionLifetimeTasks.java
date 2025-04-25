package com.sinergise.geopedia.server;

import java.util.HashMap;

import com.sinergise.geopedia.app.session.Session;
import com.sinergise.geopedia.app.session.SessionDestroyedListener;

public class SessionLifetimeTasks<T extends SessionTask> {
	private HashMap<String, T> sessionTasks = new HashMap<String, T>();
	
	
	private SessionDestroyedListener sessDestroyedListener = new SessionDestroyedListener() {

		@Override
		public void onSessionDestroyed(Session ses) {
			synchronized (sessionTasks) {
				SessionTask fet = sessionTasks.get(ses.getID());
				if (fet != null) {
					synchronized (fet) {
						purgeDoneTasks(fet, ses, true);
					}
				}
			}

		}
	};
	
	public T getTask(Session session) {
		synchronized (sessionTasks) {
			return sessionTasks.get(session.getID());
		}
	}
	
	public void addTask(Session session, T task) {
		synchronized (sessionTasks) {
			sessionTasks.put(session.getID(), task);
			session.addDestroyedListener(sessDestroyedListener);
		}
	}
	
	public boolean  purgeDoneTasks(SessionTask task, Session session) {
		return purgeDoneTasks(task, session, false);
	}

	
	public boolean purgeDoneTasks(SessionTask task, Session session,
			boolean force) {
		if (!task.isFinished() && !force)
			return false;
		task.cleanup();
		synchronized (sessionTasks) {
			sessionTasks.remove(session.getID());
			session.removeDestroyedListener(sessDestroyedListener);
		}
		return true;
	}

}
