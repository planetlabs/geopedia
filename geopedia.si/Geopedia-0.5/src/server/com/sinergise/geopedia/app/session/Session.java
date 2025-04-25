package com.sinergise.geopedia.app.session;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.xml.sax.SAXException;

import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.app.Main;
import com.sinergise.geopedia.core.constants.Globals;
import com.sinergise.geopedia.core.entities.Translation.Language;
import com.sinergise.geopedia.core.entities.User;
import com.sinergise.geopedia.core.exceptions.GeopediaException;
import com.sinergise.geopedia.core.filter.AbstractFilter;
import com.sinergise.geopedia.db.DB;
import com.sinergise.java.util.state.StateUtilJava;

public class Session {
	public static final String STATE_KEY_TIMESTAMP="timestamp";
	public static final String STATE_KEY_USERID="userID";
	public static final String STATE_KEY_LANGUAGEID="languageID";
	public static final String STATE_KEY_SERVERID="serverID";
	public static final String STATE_KEY_CONFIGID="cfgID";
	
	public static final Session DEFAULT_SESSION = new Session("DEFAULT", ServerInstance.INSTANCE_ID_GEOPEDIASI);
	
	final String sessionID;
	/** each time session content changes timestamp is increased */
	private long timestamp; 
	/** session state*/
	private StateGWT sessionState;	
	private User user = User.NO_USER;
	private Language language = Language.SI;
	private long lastUpdateTS;
	/** geopedia instance configurationID **/
	private int configurationID;
	
	private AbstractFilter[] filters;
	/**
	 * In theory  there's only one master session object on the whole network (the one created with SessionService.createSession)
	 * 
	 */
	private boolean master=false;
	
	/**
	 * 
	 */
	private ArrayList<SessionDestroyedListener> destroyedListeners = null;
	private Boolean syncLock = new Boolean(true);
	
	public void addDestroyedListener (SessionDestroyedListener sdl) {
		synchronized (syncLock) {
			if (destroyedListeners==null)
				destroyedListeners=new ArrayList<SessionDestroyedListener>();
			destroyedListeners.add(sdl);
		}
	}
	
	public void removeDestroyedListener (SessionDestroyedListener sdl) {
		synchronized (syncLock) {
			if (destroyedListeners==null)
				return;
			destroyedListeners.remove(sdl);
		}
	}
	
	public void notifyDestroyedListeners() {
		if (destroyedListeners==null)
			return;
		SessionDestroyedListener[] ll;
		synchronized(destroyedListeners) {
			ll = destroyedListeners.toArray(new SessionDestroyedListener[destroyedListeners.size()]);
		}
		for (SessionDestroyedListener sdl:ll) {
			sdl.onSessionDestroyed(this);
		}
	}
	
	public static Session createFromString(String sessionID, String stateString, long lastUpdatedTS, DB db) {
		if (stateString != null && stateString.length() != 0) {
			try {
				Session s = new Session(sessionID, stateString, lastUpdatedTS, db);				
				return s;
			} catch (Exception e) {
				// TODO handle exception properly
				e.printStackTrace();
			}
		}
		return null;
	}
	
	protected Session(String sessionID, String stateString, long lastUpdatedTS, DB db) throws IOException, SAXException {
		this.sessionID = sessionID;
		this.lastUpdateTS  = lastUpdatedTS;
		sessionState = StateUtilJava.gwtFromJavaString(stateString);
		timestamp = sessionState.getLong(STATE_KEY_TIMESTAMP, 0);

		int userId = sessionState.getInt(STATE_KEY_USERID, Integer.MIN_VALUE);
		if (userId != Integer.MIN_VALUE) {
			try {
				user = db.getUser(userId);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			user = User.NO_USER;
		}
		configurationID = sessionState.getInt(STATE_KEY_CONFIGID, 0);
		int langId = sessionState.getInt(STATE_KEY_LANGUAGEID, Integer.MIN_VALUE);
		if (langId != Integer.MIN_VALUE) {
			language = Language.get(langId);
		}
		
		filters=AbstractFilter.createFilters(sessionState);
	}
	@Deprecated
	public Session (String cookieName, String sessionId, int instanceId) {
		this(sessionId,instanceId);
	}
	public Session (String sessionId, int instanceId) {
		this.sessionID=sessionId;
		sessionState = new StateGWT();
		master=true;
		timestamp=0;
		lastUpdateTS = System.currentTimeMillis();
		configurationID=instanceId;
	}

	
	public boolean ping() {
		long now = System.currentTimeMillis();
		if (lastUpdateTS+Globals.SESSION_UPDATE_MINIMUM<=now) {
			lastUpdateTS = now;
			return true;
		}
		return false;
	}
	
	
	public String getStateString() {
		sessionState.putLong(STATE_KEY_TIMESTAMP, timestamp);
		if (user!=null) {
			sessionState.putInt(STATE_KEY_USERID, user.getId());
		}	
		sessionState.putInt(STATE_KEY_CONFIGID, configurationID);
		sessionState.putInt(STATE_KEY_LANGUAGEID, language.key());
		sessionState.putString(STATE_KEY_SERVERID, Main.getServerID());		
		AbstractFilter.storeFilters(filters,sessionState);
		
		if (sessionState==null || sessionState.isEmpty())
			return null;

		String stateStr = null;
		try {
			stateStr = StateUtilJava.javaStringFromGWT(sessionState);
		} catch (Exception e) {
			//TODO handle exception properly
			e.printStackTrace();
		}
		return stateStr;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public String getID() {
		return sessionID;
	}
	
	public void updateTimestamp() {
		timestamp++;
		lastUpdateTS = System.currentTimeMillis();
	}

	public boolean isMaster() {
		return master;
	}
	
	public void setMaster() {
		master = true;
	}
	
	// TODO: rename to getHeaderValue
	public String getSessionHeaderValue() {
		return sessionID+"_"+Long.toString(timestamp);
	}
	
	public void setUser(User u) {
		this.user=u;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setLanguage(Language language) {
		this.language = language;
	}
	
	public Language getLanguage() {
		return language;
	}
	
	public boolean isFilterByLanguage() {
		return false;
	}

	public long getLastUpdatedTS() {
		return lastUpdateTS;
	}
	
	public void setFilters(AbstractFilter filters[]) {
		this.filters=filters;
	}
	
	public AbstractFilter[] getFilters() {
		return filters;
	}
	

	public int getInstanceId() {
		return configurationID;
	}
	
	public ServerInstance getServerInstance() throws GeopediaException {
		return ServerInstance.getInstance(getInstanceId());
	}
}
