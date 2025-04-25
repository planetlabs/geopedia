package com.sinergise.geopedia.app.session;


import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinergise.geopedia.ServerInstance;
import com.sinergise.geopedia.core.entities.User;
import com.sinergise.geopedia.core.exceptions.GeopediaException;

public class SessionUtils {

	public static final Logger logger = LoggerFactory.getLogger(SessionUtils.class);
	
	public static User login(Session sess, int userID) throws GeopediaException {
		if (sess == null)
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		synchronized(sess) {
			if (!sess.getUser().equals(User.NO_USER))
				throw new GeopediaException(GeopediaException.Type.INVALID_USER_STATE);
			
			User u;
			try {
				u = instance.getDB().getUser(userID);
				if (u == null)
					throw new GeopediaException(GeopediaException.Type.INVALID_USER);
			} catch (SQLException e) {
				logger.error("DB error while login!", e);
				throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
			}
			
			
			sess.setUser(u);
			SessionStorage.updateSession(sess);
            return u;
		}	
	}
    
	public static User login(Session sess, String username, String password) throws GeopediaException
    {
		if (sess == null)
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);
		ServerInstance instance = ServerInstance.getInstance(sess.getInstanceId());
		synchronized(sess) {
			if (sess.getUser()!=null && sess.getUser().isLoggedIn())
				throw new GeopediaException(GeopediaException.Type.INVALID_USER_STATE);
			
			User u;
			try {
				u = instance.getDB().getUser(username, password);
				if (u == null)
					throw new GeopediaException(GeopediaException.Type.INVALID_USER);
			} catch (SQLException e) {
				logger.error("DB error while login!", e);
				throw new GeopediaException(GeopediaException.Type.DATABASE_ERROR);
			}
			
			
			sess.setUser(u);
			SessionStorage.updateSession(sess);
			
//			EventData loginEvent = new EventData();
//			loginEvent.setEventType(SGEventConst.TYPE_USER_LOGIN);
//			loginEvent.put(SGEventConst.ATTR_USER_NAME, u.login);
//			loginEvent.put(SGEventConst.ATTR_USER_ID, u.id);
//			SGEventLogger.infoEvent(SGMarkers.EVENT_INFO, loginEvent);
            return u;
		}
    }

	public static void logout(Session sess) throws GeopediaException {
		if (sess == null)
			throw new GeopediaException(GeopediaException.Type.NO_SESSION);

		synchronized(sess) {
			User user = sess.getUser();
			if (user==null || !user.isLoggedIn())
				throw new GeopediaException(GeopediaException.Type.INVALID_USER_STATE);
			
			sess.setUser(User.NO_USER);
			SessionStorage.updateSession(sess);
//			EventData loginEvent = new EventData();
//			loginEvent.setEventType(SGEventConst.TYPE_USER_LOGOUT);
//			loginEvent.put(SGEventConst.ATTR_USER_NAME, user.login);
//			loginEvent.put(SGEventConst.ATTR_USER_ID, user.id);
//			SGEventLogger.infoEvent(SGMarkers.EVENT_INFO, loginEvent);
		}			
	}

}
