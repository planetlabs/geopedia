package com.sinergise.geopedia.app.session;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.Main;
import com.sinergise.geopedia.app.Periodic;
import com.sinergise.geopedia.core.constants.Globals;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.db.DB;
import com.sinergise.util.MD5;

public class SessionStorage {

	
	static Object2ObjectOpenHashMap<String,Session> sessionCache = new Object2ObjectOpenHashMap<String, Session>();
	static final String servermd5 = MD5.hash32(Main.getServerID()); //TODO: checkMD5
	static final Random r = new Random();
	private final static Object lock = new Object();
	
	public static Session getSession(String sessionID, int instanceId, long timestamp) throws GeopediaException{
		Session session = null;
		ServerInstance instance = ServerInstance.getInstance(instanceId);
		synchronized(lock) {
			session = sessionCache.get(sessionID);
			if (session == null || timestamp > session.getTimestamp()) {
				// load session from database
				try {
					long maxAge = System.currentTimeMillis()-Globals.SESSION_TIMEOUT;
					Session sess = instance.getDB().loadSession(sessionID, maxAge);
					if (sess==null) // nonexisting session
						return null;					
					sessionCache.put(sessionID, sess);
					return sess;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return session;
	}
	
	
	public static void updateSession(Session session) throws GeopediaException{		
		Session ses = sessionCache.get(session.getID());
		if (ses==null) {
			//TODO: exception!
			return ;
		}
		ServerInstance instance = ServerInstance.getInstance(ses.getInstanceId());
		session.updateTimestamp();
		try {
			instance.getDB().updateSessionState(session.getID(),session.getStateString());
		} catch (SQLException e) {
			//TODO: exception!
			e.printStackTrace();
		}
	}

	@Deprecated
	public static Session createNewSession(String cookieName, int instanceId) throws GeopediaException {
		return createNewSession(instanceId);
	}

	public static Session createNewSession(int instanceId) throws GeopediaException {
		return createNewSession(instanceId, true, false);
	}
	
	@Deprecated
	public static Session createNewSession(String cookieName, int instanceId, boolean isGlobal, boolean isOnetime) throws GeopediaException {
		return createNewSession(instanceId, isGlobal, isOnetime);
	}

	public static Session createNewSession(int instanceId, boolean isGlobal, boolean isOnetime) throws GeopediaException {
		
		Session session = null;
		ServerInstance instance = ServerInstance.getInstance(instanceId);
		DB db = instance.getDB();
		synchronized(lock) {
			String sid = servermd5 + MD5.hash32(String.valueOf(r.nextLong()));//TODO: checkMD5
			session = new Session(sid, instanceId);
			if (isGlobal) {
				try {
					db.addSessionState(sid, session.getStateString());
				} catch (SQLException e) {
					//TODO: exception!
					e.printStackTrace();
					return null;
				}
			}
			if (!isOnetime)
				sessionCache.put(sid, session);
		}
		
		return session;
	}
	
	public static void purgeDeadSessions(long timeout) {
		long olderThanTimestamp = System.currentTimeMillis() - timeout;
		ArrayList<Session> destroyed = new ArrayList<Session>();
		ServerInstance instance = null;
		
		for (ServerInstance inst:ServerInstance.allInstances()) {
			if (inst!=null) {
				instance = inst;
				break;
			}
		}
		if (instance==null) return;	
		
		synchronized(lock) {
			ObjectIterator<Session> sesIt = sessionCache.values().iterator();
			while (sesIt.hasNext()) {
				Session ses = sesIt.next();
				String sid = ses.getID();
				if (ses.getLastUpdatedTS()<olderThanTimestamp) {
					sessionCache.remove(sid);
					destroyed.add(ses);
					if (ses.isMaster()) {
						try {
							instance.getDB().deleteSession(sid);
						} catch (Exception ex) {
							// silently ignore.
						}
					}
				}
			}
		}
		for (Session ses:destroyed)
			ses.notifyDestroyedListeners();
		
	}
	
	static {
		Periodic.add(new Periodic.Task() {
			public boolean execute()
			{
				purgeDeadSessions(Globals.SESSION_TIMEOUT);
				return true;
			}
		}, 10000);
	}
}
